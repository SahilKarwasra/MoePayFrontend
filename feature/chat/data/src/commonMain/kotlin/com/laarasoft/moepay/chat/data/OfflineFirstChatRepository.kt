package com.laarasoft.moepay.chat.data

import com.laarasoft.moepay.chat.data.remote.ChatPaymentRequest
import com.laarasoft.moepay.chat.data.remote.ChatRemoteDataSource
import com.laarasoft.moepay.chat.data.remote.toDomain
import com.laarasoft.moepay.chat.data.sync.ChatSyncEngine
import com.laarasoft.moepay.chat.data.sync.DefaultChatEngine
import com.laarasoft.moepay.chat.domain.ChatConnection
import com.laarasoft.moepay.chat.domain.ChatLocalDataSource
import com.laarasoft.moepay.chat.domain.ChatMessage
import com.laarasoft.moepay.chat.domain.ChatRepository
import com.laarasoft.moepay.chat.domain.Conversation
import com.laarasoft.moepay.chat.domain.Delivery
import com.laarasoft.moepay.chat.domain.MessageContent
import com.laarasoft.moepay.chat.domain.OutboxOperation
import com.laarasoft.moepay.chat.domain.OutboxType
import com.laarasoft.moepay.core.data.di.ApplicationScope
import com.laarasoft.moepay.core.data.money.toDto
import com.laarasoft.moepay.core.data.networking.retryIdempotent
import com.laarasoft.moepay.core.data.wallet.asRemoteResult
import com.laarasoft.moepay.core.data.wallet.asWalletResult
import com.laarasoft.moepay.core.domain.auth.SessionStorage
import com.laarasoft.moepay.core.domain.connectivity.ConnectivityObserver
import com.laarasoft.moepay.core.domain.money.Money
import com.laarasoft.moepay.core.domain.money.MoneyRequestStatus
import com.laarasoft.moepay.core.domain.money.RecipientRef
import com.laarasoft.moepay.core.domain.realtime.RealtimeConnection
import com.laarasoft.moepay.core.domain.realtime.RealtimeEvents
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.EmptyResult
import com.laarasoft.moepay.core.domain.util.Result
import com.laarasoft.moepay.core.domain.wallet.IdempotencyKey
import com.laarasoft.moepay.core.domain.wallet.WalletError
import com.laarasoft.moepay.core.domain.wallet.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.uuid.Uuid

class OfflineFirstChatRepository(
    private val local: ChatLocalDataSource,
    private val remote: ChatRemoteDataSource,
    private val sync: ChatSyncEngine,
    private val engine: DefaultChatEngine,
    private val sessionStorage: SessionStorage,
    private val walletRepository: WalletRepository,
    private val json: Json,
    realtime: RealtimeEvents,
    connectivity: ConnectivityObserver,
    applicationScope: ApplicationScope,
    private val clock: Clock = Clock.System,
) : ChatRepository {

    override val connection: StateFlow<ChatConnection> =
        combine(connectivity.isOnline, realtime.connection) { online, socket ->
            when {
                !online -> ChatConnection.OFFLINE
                socket == RealtimeConnection.CONNECTED -> ChatConnection.ONLINE
                else -> ChatConnection.CONNECTING
            }
        }.stateIn(applicationScope, SharingStarted.Eagerly, ChatConnection.CONNECTING)

    override fun observeConversations(): Flow<List<Conversation>> = local.observeConversations()

    override fun observeConversation(conversationId: String): Flow<Conversation?> = local.observeConversation(conversationId)

    override fun observeMessages(conversationId: String, limit: Int): Flow<List<ChatMessage>> =
        local.observeMessages(conversationId, limit)

    override fun observeTotalUnread(): Flow<Int> = local.observeTotalUnread()

    override suspend fun refresh(): EmptyResult<DataError.Remote> {
        engine.wakeOutbox()
        return sync.pull()
    }

    override suspend fun openConversation(with: RecipientRef): Result<Conversation, DataError.Remote> =
        when (val result = remote.open(with.toDto())) {
            is Result.Failure -> result
            is Result.Success -> {
                val conversation = result.data.toDomain()
                local.upsertServerConversation(conversation)
                Result.Success(conversation)
            }
        }

    override suspend fun loadOlder(conversationId: String): Result<Boolean, DataError.Remote> {
        val oldest = local.oldestSeq(conversationId) ?: return Result.Success(false)
        if (oldest <= 1) return Result.Success(false)
        return when (val result = remote.messagesBefore(conversationId, beforeSeq = oldest, limit = 50).asRemoteResult()) {
            is Result.Failure -> result
            is Result.Success -> {
                local.upsertServerMessages(result.data.items.map { it.toDomain() })
                Result.Success(result.data.hasMore)
            }
        }
    }

    override suspend fun sendText(conversationId: String, body: String, replyToId: String?) {
        val text = body.trim()
        if (text.isEmpty()) return
        val now = clock.now()
        val localId = Uuid.random().toString()
        local.insertPending(
            message = pendingMessage(localId, conversationId, MessageContent.Text(text), replyToId),
            operation = OutboxOperation(
                type = OutboxType.SEND_TEXT,
                conversationId = conversationId,
                localId = localId,
                body = text,
                replyToId = replyToId,
                nextAttemptAt = now,
                createdAt = now,
            ),
        )
        local.saveDraft(conversationId, "")
        engine.wakeOutbox()
    }

    override suspend fun requestMoney(conversationId: String, amountPence: Long, note: String?) {
        val now = clock.now()
        val localId = Uuid.random().toString()
        val cleanNote = note?.trim()?.takeIf { it.isNotEmpty() }
        val content = MessageContent.Request(
            moneyRequestId = "",
            amount = Money(amountPence, formatGbp(amountPence)),
            isOutgoing = true,
            status = MoneyRequestStatus.PENDING,
            actions = emptySet(),
            expiresAt = null,
            note = cleanNote,
        )
        local.insertPending(
            message = pendingMessage(localId, conversationId, content, replyToId = null),
            operation = OutboxOperation(
                type = OutboxType.SEND_REQUEST,
                conversationId = conversationId,
                localId = localId,
                body = cleanNote,
                amountPence = amountPence,
                nextAttemptAt = now,
                createdAt = now,
            ),
        )
        engine.wakeOutbox()
    }

    override suspend fun retry(localId: String) {
        val message = local.getMessage(localId) ?: return
        if (message.delivery != Delivery.FAILED) return
        val now = clock.now()
        val operation = when (val content = message.content) {
            is MessageContent.Text -> OutboxOperation(
                type = OutboxType.SEND_TEXT,
                conversationId = message.conversationId,
                localId = localId,
                body = content.body,
                replyToId = message.replyToId,
                nextAttemptAt = now,
                createdAt = now,
            )
            is MessageContent.Request -> OutboxOperation(
                type = OutboxType.SEND_REQUEST,
                conversationId = message.conversationId,
                localId = localId,
                body = content.note,
                amountPence = content.amount.pence,
                nextAttemptAt = now,
                createdAt = now,
            )
            else -> return
        }
        local.insertPending(message.copy(delivery = Delivery.PENDING, failure = null, createdAt = now), operation)
        engine.wakeOutbox()
    }

    override suspend fun discard(localId: String) {
        local.removeOperationsFor(localId)
        local.deleteMessage(localId)
    }

    override suspend fun edit(localId: String, body: String) {
        val text = body.trim()
        val message = local.getMessage(localId) ?: return
        val current = message.content as? MessageContent.Text ?: return
        if (text.isEmpty() || text == current.body || !message.isSent) return
        val now = clock.now()
        // A second edit before the first one went out replaces it: one request, latest text.
        val queued = local.pendingOperations().lastOrNull { it.type == OutboxType.EDIT && it.localId == localId }
        local.updateMessage(message.copy(content = MessageContent.Text(text), isEdited = true))
        if (queued != null) {
            local.updateOperation(queued.copy(body = text))
        } else {
            local.enqueue(
                OutboxOperation(
                    type = OutboxType.EDIT,
                    conversationId = message.conversationId,
                    localId = localId,
                    body = text,
                    baseVersion = message.version,
                    nextAttemptAt = now,
                    createdAt = now,
                )
            )
        }
        engine.wakeOutbox()
    }

    override suspend fun delete(localId: String) {
        val message = local.getMessage(localId) ?: return
        if (!message.isSent) {
            discard(localId)
            return
        }
        val now = clock.now()
        local.removeOperationsFor(localId)
        local.updateMessage(message.copy(content = MessageContent.Deleted))
        local.enqueue(
            OutboxOperation(
                type = OutboxType.DELETE,
                conversationId = message.conversationId,
                localId = localId,
                nextAttemptAt = now,
                createdAt = now,
            )
        )
        engine.wakeOutbox()
    }

    override suspend fun markRead(conversationId: String, upToSeq: Long) {
        val conversation = local.observeConversation(conversationId).firstOrNull() ?: return
        if (upToSeq <= conversation.myLastReadSeq) return
        val now = clock.now()
        local.markReadLocally(conversationId, upToSeq)
        local.enqueueMarkRead(
            OutboxOperation(
                type = OutboxType.MARK_READ,
                conversationId = conversationId,
                upToSeq = upToSeq,
                nextAttemptAt = now,
                createdAt = now,
            )
        )
        engine.wakeOutbox()
    }

    override suspend fun saveDraft(conversationId: String, text: String) = local.saveDraft(conversationId, text)

    override suspend fun sendMoney(
        conversationId: String,
        key: IdempotencyKey,
        amountPence: Long,
        note: String?,
        pin: String,
    ): Result<ChatMessage, WalletError> {
        val request = ChatPaymentRequest(
            clientMessageId = key.value,
            amountPence = amountPence,
            note = note?.trim()?.takeIf { it.isNotEmpty() },
            pin = pin,
        )
        val result = retryIdempotent { remote.pay(conversationId, key, request).asWalletResult(json) }
        return when (result) {
            is Result.Failure -> result
            is Result.Success -> {
                val message = result.data.message.toDomain()
                local.upsertServerMessages(listOf(message))
                walletRepository.refresh()
                Result.Success(message)
            }
        }
    }

    private suspend fun pendingMessage(
        localId: String,
        conversationId: String,
        content: MessageContent,
        replyToId: String?,
    ): ChatMessage = ChatMessage(
        localId = localId,
        serverId = null,
        conversationId = conversationId,
        seq = null,
        senderId = sessionStorage.observeAuthInfo().firstOrNull()?.user?.id.orEmpty(),
        isMine = true,
        content = content,
        replyToId = replyToId,
        version = 0,
        isEdited = false,
        createdAt = clock.now(),
        delivery = Delivery.PENDING,
    )
}

/** "£1,234.50" for a request shown before the server has formatted it. */
internal fun formatGbp(pence: Long): String {
    val pounds = (pence / 100).toString().reversed().chunked(3).joinToString(",").reversed()
    return "£$pounds.${(pence % 100).toString().padStart(2, '0')}"
}
