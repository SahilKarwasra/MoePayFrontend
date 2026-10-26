package com.laarasoft.moepay.chat.database

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.laarasoft.moepay.chat.database.entity.SyncStateEntity
import com.laarasoft.moepay.chat.domain.ChatLocalDataSource
import com.laarasoft.moepay.chat.domain.ChatMessage
import com.laarasoft.moepay.chat.domain.Conversation
import com.laarasoft.moepay.chat.domain.OutboxOperation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class RoomChatLocalDataSource(
    private val database: ChatDatabase,
) : ChatLocalDataSource {

    private val conversations = database.conversationDao
    private val messages = database.messageDao
    private val outbox = database.outboxDao
    private val syncState = database.syncStateDao

    override fun observeConversations(): Flow<List<Conversation>> =
        combine(conversations.observeAll(), messages.observeLatestPerConversation()) { rows, latest ->
            val latestByConversation = latest.associateBy { it.conversationId }
            rows.map { it.toDomain(latestByConversation[it.id]?.toDomain()) }
                .sortedByDescending { it.lastActivityAt }
        }.distinctUntilChanged()

    override fun observeConversation(conversationId: String): Flow<Conversation?> =
        combine(conversations.observe(conversationId), messages.observeNewest(conversationId, 1)) { row, newest ->
            row?.toDomain(newest.firstOrNull()?.toDomain())
        }.distinctUntilChanged()

    override fun observeMessages(conversationId: String, limit: Int): Flow<List<ChatMessage>> =
        messages.observeNewest(conversationId, limit)
            .map { rows -> rows.map { it.toDomain() } }
            .distinctUntilChanged()

    override fun observeTotalUnread(): Flow<Int> = conversations.observeTotalUnread().distinctUntilChanged()

    override suspend fun syncCursor(): Long = syncState.cursor() ?: 0L

    override suspend fun applyChanges(conversations: List<Conversation>, messages: List<ChatMessage>, cursor: Long) {
        transaction {
            conversations.forEach { mergeConversation(it) }
            messages.forEach { mergeMessage(it) }
            syncState.upsert(SyncStateEntity(cursor = cursor))
        }
    }

    override suspend fun upsertServerMessages(messages: List<ChatMessage>) {
        transaction { messages.forEach { mergeMessage(it) } }
    }

    override suspend fun upsertServerConversation(conversation: Conversation) {
        transaction { mergeConversation(conversation) }
    }

    /**
     * Server data wins, except where the device knows better: the local draft, a read pointer
     * the server hasn't heard about yet, and edits or deletes still waiting in the outbox.
     */
    private suspend fun mergeConversation(conversation: Conversation) {
        val existing = conversations.get(conversation.id)
        val myLastReadSeq = maxOf(existing?.myLastReadSeq ?: 0L, conversation.myLastReadSeq)
        val unread = if (myLastReadSeq > conversation.myLastReadSeq) {
            messages.countFromPeerAfter(conversation.id, myLastReadSeq)
        } else {
            conversation.unreadCount
        }
        conversations.upsert(
            conversation
                .copy(myLastReadSeq = myLastReadSeq, unreadCount = unread)
                .toEntity(draft = existing?.draft.orEmpty())
        )
        conversation.lastMessage?.let { mergeMessage(it) }
    }

    private suspend fun mergeMessage(message: ChatMessage) {
        val existing = messages.get(message.localId) ?: message.serverId?.let { messages.getByServerId(it) }
        if (existing == null) {
            messages.upsert(message.toEntity())
            return
        }
        // An older copy (e.g. a late sync page) never replaces a newer one.
        if (existing.serverId != null && existing.version > message.version) return
        // The user's own edit or delete is shown until the server has it.
        if (outbox.countLocalChanges(existing.localId) > 0) return
        messages.upsert(message.toEntity().copy(localId = existing.localId))
    }

    override suspend fun getMessage(localId: String): ChatMessage? = messages.get(localId)?.toDomain()

    override suspend fun oldestSeq(conversationId: String): Long? = messages.oldestSeq(conversationId)

    override suspend fun insertPending(message: ChatMessage, operation: OutboxOperation) {
        transaction {
            messages.upsert(message.toEntity())
            outbox.upsert(operation.toEntity())
        }
    }

    override suspend fun updateMessage(message: ChatMessage) = messages.upsert(message.toEntity())

    override suspend fun deleteMessage(localId: String) = messages.delete(localId)

    override suspend fun enqueue(operation: OutboxOperation) = outbox.upsert(operation.toEntity())

    override suspend fun enqueueMarkRead(operation: OutboxOperation) {
        transaction {
            val existing = outbox.markReadFor(operation.conversationId)
            if (existing == null) {
                outbox.upsert(operation.toEntity())
            } else {
                val upTo = maxOf(existing.upToSeq ?: 0L, operation.upToSeq ?: 0L)
                outbox.upsert(existing.copy(upToSeq = upTo, attempts = 0, nextAttemptAt = operation.nextAttemptAt.toEpochMilliseconds()))
            }
        }
    }

    override suspend fun markReadLocally(conversationId: String, upToSeq: Long) {
        transaction {
            val conversation = conversations.get(conversationId) ?: return@transaction
            if (upToSeq <= conversation.myLastReadSeq) return@transaction
            conversations.updateRead(conversationId, upToSeq, messages.countFromPeerAfter(conversationId, upToSeq))
        }
    }

    override suspend fun pendingOperations(): List<OutboxOperation> = outbox.all().map { it.toDomain() }

    override fun observePendingCount(): Flow<Int> = outbox.observeCount()

    override suspend fun updateOperation(operation: OutboxOperation) = outbox.upsert(operation.toEntity())

    override suspend fun removeOperation(id: Long) = outbox.delete(id)

    override suspend fun removeOperationsFor(localId: String) = outbox.deleteFor(localId)

    override suspend fun saveDraft(conversationId: String, text: String) = conversations.updateDraft(conversationId, text)

    override suspend fun clear() {
        transaction {
            outbox.clear()
            messages.clear()
            conversations.clear()
            syncState.clear()
        }
    }

    private suspend fun transaction(block: suspend () -> Unit) {
        database.useWriterConnection { transactor -> transactor.immediateTransaction { block() } }
    }
}
