package com.laarasoft.moepay.chat.domain

import com.laarasoft.moepay.core.domain.money.RecipientRef
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.EmptyResult
import com.laarasoft.moepay.core.domain.util.Result
import com.laarasoft.moepay.core.domain.wallet.IdempotencyKey
import com.laarasoft.moepay.core.domain.wallet.WalletError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Offline-first chat. Everything is read from the device database; writes are saved
 * locally first and delivered by the outbox when the server is reachable.
 */
interface ChatRepository {
    /** Most recent activity first. */
    fun observeConversations(): Flow<List<Conversation>>

    fun observeConversation(conversationId: String): Flow<Conversation?>

    /** Newest first, at most [limit] messages. */
    fun observeMessages(conversationId: String, limit: Int): Flow<List<ChatMessage>>

    /** Unread messages across all conversations, for the tab badge. */
    fun observeTotalUnread(): Flow<Int>

    val connection: StateFlow<ChatConnection>

    /** Pulls every change since the last sync. */
    suspend fun refresh(): EmptyResult<DataError.Remote>

    /** Finds or creates the conversation with someone. Needs the network. */
    suspend fun openConversation(with: RecipientRef): Result<Conversation, DataError.Remote>

    /** Fetches messages older than the oldest one on the device, when there are any. */
    suspend fun loadOlder(conversationId: String): Result<Boolean, DataError.Remote>

    suspend fun sendText(conversationId: String, body: String, replyToId: String?)

    suspend fun requestMoney(conversationId: String, amountPence: Long, note: String?)

    /** Re-queues a message that failed to send. */
    suspend fun retry(localId: String)

    /** Drops a message that failed to send. */
    suspend fun discard(localId: String)

    suspend fun edit(localId: String, body: String)

    suspend fun delete(localId: String)

    /** Marks everything up to [upToSeq] as read, locally at once and on the server later. */
    suspend fun markRead(conversationId: String, upToSeq: Long)

    suspend fun saveDraft(conversationId: String, text: String)

    /**
     * Pays the other member. Needs the network and the PIN (never stored), so unlike text
     * it is not queued. The [key] is also the message's client id, making retries safe.
     */
    suspend fun sendMoney(
        conversationId: String,
        key: IdempotencyKey,
        amountPence: Long,
        note: String?,
        pin: String,
    ): Result<ChatMessage, WalletError>
}

/** Ephemeral "typing…" state; never stored. */
interface TypingIndicator {
    /** Conversation ids where the other member is typing right now. */
    val typingConversations: StateFlow<Set<String>>

    /** Call on every keystroke; sends are throttled. */
    fun onUserTyping(conversationId: String)

    fun onUserStoppedTyping(conversationId: String)
}

/** Runs sync and the outbox while signed in and in the foreground. */
interface ChatEngine {
    fun start()
    fun stop()

    /** Stops and wipes the device's chat data (sign-out). */
    suspend fun clear()
}
