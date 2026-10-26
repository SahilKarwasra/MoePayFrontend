package com.laarasoft.moepay.chat.domain

import kotlinx.coroutines.flow.Flow

/** The device database. Server data is merged in; local-only state (drafts, pending) is kept. */
interface ChatLocalDataSource {
    fun observeConversations(): Flow<List<Conversation>>
    fun observeConversation(conversationId: String): Flow<Conversation?>
    fun observeMessages(conversationId: String, limit: Int): Flow<List<ChatMessage>>
    fun observeTotalUnread(): Flow<Int>

    suspend fun syncCursor(): Long

    /**
     * Applies one page of server changes atomically and moves the cursor to [cursor],
     * so a crash can never skip changes.
     */
    suspend fun applyChanges(conversations: List<Conversation>, messages: List<ChatMessage>, cursor: Long)

    /** Merges server messages (newer versions win; local drafts and pending rows are kept). */
    suspend fun upsertServerMessages(messages: List<ChatMessage>)

    suspend fun upsertServerConversation(conversation: Conversation)

    suspend fun getMessage(localId: String): ChatMessage?

    /** Oldest server seq on the device for a conversation, or null when there are none. */
    suspend fun oldestSeq(conversationId: String): Long?

    /** Saves a message typed on the device together with the operation that will send it. */
    suspend fun insertPending(message: ChatMessage, operation: OutboxOperation)

    suspend fun updateMessage(message: ChatMessage)

    suspend fun deleteMessage(localId: String)

    suspend fun enqueue(operation: OutboxOperation)

    /** Keeps one read operation per conversation, with the highest seq. */
    suspend fun enqueueMarkRead(operation: OutboxOperation)

    suspend fun markReadLocally(conversationId: String, upToSeq: Long)

    /** Operations in the order they were made. */
    suspend fun pendingOperations(): List<OutboxOperation>

    fun observePendingCount(): Flow<Int>

    suspend fun updateOperation(operation: OutboxOperation)

    suspend fun removeOperation(id: Long)

    suspend fun removeOperationsFor(localId: String)

    suspend fun saveDraft(conversationId: String, text: String)

    /** Wipes everything (sign-out). */
    suspend fun clear()
}
