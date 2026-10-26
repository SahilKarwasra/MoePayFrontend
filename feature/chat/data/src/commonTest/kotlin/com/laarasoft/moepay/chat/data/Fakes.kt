package com.laarasoft.moepay.chat.data

import com.laarasoft.moepay.chat.data.remote.ChatMoneyRequestRequest
import com.laarasoft.moepay.chat.data.remote.ChatPaymentRequest
import com.laarasoft.moepay.chat.data.remote.ChatRemoteDataSource
import com.laarasoft.moepay.chat.data.remote.ConversationDto
import com.laarasoft.moepay.chat.data.remote.EditMessageRequest
import com.laarasoft.moepay.chat.data.remote.MessageDto
import com.laarasoft.moepay.chat.data.remote.MessagePageDto
import com.laarasoft.moepay.chat.data.remote.MessageResultDto
import com.laarasoft.moepay.chat.data.remote.PeerDto
import com.laarasoft.moepay.chat.data.remote.SendMessageRequest
import com.laarasoft.moepay.chat.data.remote.SyncPageDto
import com.laarasoft.moepay.chat.domain.ChatLocalDataSource
import com.laarasoft.moepay.chat.domain.ChatMessage
import com.laarasoft.moepay.chat.domain.Conversation
import com.laarasoft.moepay.chat.domain.OutboxOperation
import com.laarasoft.moepay.chat.domain.OutboxType
import com.laarasoft.moepay.core.data.money.RecipientRefDto
import com.laarasoft.moepay.core.data.networking.ApiFailure
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Result
import com.laarasoft.moepay.core.domain.wallet.IdempotencyKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

internal fun messageDto(
    id: String,
    seq: Long,
    clientId: String? = null,
    mine: Boolean = true,
    body: String = "hello",
    version: Int = 1,
) = MessageDto(
    id = id, conversationId = "c1", seq = seq, clientMessageId = clientId, senderId = if (mine) "me" else "them",
    mine = mine, kind = "text", body = body, version = version, createdAt = "2026-10-26T10:00:00Z",
)

internal fun conversationDto(id: String = "c1", lastSeq: Long = 0) = ConversationDto(
    id = id, peer = PeerDto(userId = "them", username = "alex", displayName = "Alex Taylor"),
    lastSeq = lastSeq, lastMessageAt = "2026-10-26T10:00:00Z",
)

/** In-memory stand-in for the Room database. */
internal class FakeLocal : ChatLocalDataSource {
    val messages = linkedMapOf<String, ChatMessage>()
    val conversations = linkedMapOf<String, Conversation>()
    val operations = mutableListOf<OutboxOperation>()
    var cursor = 0L
    var appliedPages = 0
    private var nextOpId = 1L
    private val changes = MutableStateFlow(0)

    override fun observeConversations(): Flow<List<Conversation>> = changes.map { conversations.values.toList() }
    override fun observeConversation(conversationId: String): Flow<Conversation?> = changes.map { conversations[conversationId] }
    override fun observeMessages(conversationId: String, limit: Int): Flow<List<ChatMessage>> =
        changes.map { messages.values.filter { it.conversationId == conversationId }.take(limit) }
    override fun observeTotalUnread(): Flow<Int> = changes.map { conversations.values.sumOf { it.unreadCount } }
    override suspend fun syncCursor(): Long = cursor

    override suspend fun applyChanges(conversations: List<Conversation>, messages: List<ChatMessage>, cursor: Long) {
        conversations.forEach { upsertServerConversation(it) }
        upsertServerMessages(messages)
        this.cursor = cursor
        appliedPages++
    }

    override suspend fun upsertServerMessages(messages: List<ChatMessage>) {
        messages.forEach { incoming ->
            val existing = this.messages[incoming.localId] ?: this.messages.values.firstOrNull { it.serverId == incoming.serverId }
            if (existing != null && existing.serverId != null && existing.version > incoming.version) return@forEach
            if (existing != null) this.messages.remove(existing.localId)
            this.messages[existing?.localId ?: incoming.localId] = incoming.copy(localId = existing?.localId ?: incoming.localId)
        }
        changes.value++
    }

    override suspend fun upsertServerConversation(conversation: Conversation) {
        conversations[conversation.id] = conversation
        changes.value++
    }

    override suspend fun getMessage(localId: String): ChatMessage? = messages[localId]
    override suspend fun oldestSeq(conversationId: String): Long? = messages.values.mapNotNull { it.seq }.minOrNull()

    override suspend fun insertPending(message: ChatMessage, operation: OutboxOperation) {
        messages[message.localId] = message
        enqueue(operation)
    }

    override suspend fun updateMessage(message: ChatMessage) {
        messages[message.localId] = message
    }

    override suspend fun deleteMessage(localId: String) {
        messages.remove(localId)
    }

    override suspend fun enqueue(operation: OutboxOperation) {
        operations += operation.copy(id = nextOpId++)
    }

    override suspend fun enqueueMarkRead(operation: OutboxOperation) {
        val index = operations.indexOfFirst { it.type == OutboxType.MARK_READ && it.conversationId == operation.conversationId }
        if (index < 0) enqueue(operation) else operations[index] = operations[index].copy(upToSeq = maxOf(operations[index].upToSeq ?: 0, operation.upToSeq ?: 0))
    }

    override suspend fun markReadLocally(conversationId: String, upToSeq: Long) {
        conversations[conversationId]?.let { conversations[conversationId] = it.copy(myLastReadSeq = upToSeq, unreadCount = 0) }
    }

    override suspend fun pendingOperations(): List<OutboxOperation> = operations.toList()
    override fun observePendingCount(): Flow<Int> = changes.map { operations.size }

    override suspend fun updateOperation(operation: OutboxOperation) {
        val index = operations.indexOfFirst { it.id == operation.id }
        if (index >= 0) operations[index] = operation
    }

    override suspend fun removeOperation(id: Long) {
        operations.removeAll { it.id == id }
    }

    override suspend fun removeOperationsFor(localId: String) {
        operations.removeAll { it.localId == localId }
    }

    override suspend fun saveDraft(conversationId: String, text: String) = Unit

    override suspend fun clear() {
        messages.clear()
        conversations.clear()
        operations.clear()
        cursor = 0
    }
}

/** Scriptable server. */
internal class FakeRemote : ChatRemoteDataSource {
    val syncPages = ArrayDeque<Result<SyncPageDto, DataError.Remote>>()
    val syncCalls = mutableListOf<Long>()
    var sendResult: (SendMessageRequest) -> Result<MessageDto, ApiFailure> = { request ->
        Result.Success(messageDto(id = "srv-${request.clientMessageId}", seq = 10, clientId = request.clientMessageId, body = request.body))
    }
    val sent = mutableListOf<SendMessageRequest>()
    var editResult: (String, EditMessageRequest) -> Result<MessageDto, ApiFailure> = { id, request ->
        Result.Success(messageDto(id = id, seq = 1, body = request.body, version = 2))
    }
    var markReadResult: Result<ConversationDto, ApiFailure> = Result.Success(conversationDto())

    override suspend fun sync(after: Long, limit: Int): Result<SyncPageDto, DataError.Remote> {
        syncCalls += after
        return syncPages.removeFirstOrNull() ?: Result.Success(SyncPageDto(nextAfter = after))
    }

    override suspend fun open(with: RecipientRefDto): Result<ConversationDto, DataError.Remote> = Result.Success(conversationDto())
    override suspend fun messagesBefore(conversationId: String, beforeSeq: Long, limit: Int): Result<MessagePageDto, ApiFailure> =
        Result.Success(MessagePageDto())

    override suspend fun send(conversationId: String, request: SendMessageRequest): Result<MessageDto, ApiFailure> {
        sent += request
        return sendResult(request)
    }

    override suspend fun edit(conversationId: String, messageId: String, request: EditMessageRequest): Result<MessageDto, ApiFailure> =
        editResult(messageId, request)

    override suspend fun delete(conversationId: String, messageId: String): Result<MessageDto, ApiFailure> =
        Result.Success(messageDto(id = messageId, seq = 1, version = 3).copy(deleted = true))

    override suspend fun markRead(conversationId: String, upToSeq: Long): Result<ConversationDto, ApiFailure> = markReadResult

    override suspend fun requestMoney(conversationId: String, request: ChatMoneyRequestRequest): Result<MessageResultDto, ApiFailure> =
        Result.Failure(ApiFailure(DataError.Remote.LIMIT_EXCEEDED))

    override suspend fun pay(conversationId: String, key: IdempotencyKey, request: ChatPaymentRequest): Result<MessageResultDto, ApiFailure> =
        Result.Failure(ApiFailure(DataError.Remote.UNKNOWN))
}

internal val epoch = Instant.parse("2026-10-26T10:00:00Z")
