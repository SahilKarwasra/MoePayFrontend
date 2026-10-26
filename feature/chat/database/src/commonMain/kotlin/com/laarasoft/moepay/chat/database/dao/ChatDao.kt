package com.laarasoft.moepay.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.laarasoft.moepay.chat.database.entity.ConversationEntity
import com.laarasoft.moepay.chat.database.entity.MessageEntity
import com.laarasoft.moepay.chat.database.entity.OutboxEntity
import com.laarasoft.moepay.chat.database.entity.SyncStateEntity
import kotlinx.coroutines.flow.Flow

/*
 * Message order: messages the server accepted come by seq; ones still pending have no seq
 * yet and are always newer, so they sort after (newest-first: before) every synced message.
 */
private const val NEWEST_FIRST = "(seq IS NULL) DESC, seq DESC, createdAt DESC"

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY lastMessageAt DESC")
    fun observeAll(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    fun observe(id: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun get(id: String): ConversationEntity?

    @Query("SELECT COALESCE(SUM(unreadCount), 0) FROM conversations")
    fun observeTotalUnread(): Flow<Int>

    @Upsert
    suspend fun upsert(conversation: ConversationEntity)

    @Query("UPDATE conversations SET draft = :draft WHERE id = :id")
    suspend fun updateDraft(id: String, draft: String)

    @Query("UPDATE conversations SET myLastReadSeq = :seq, unreadCount = :unread WHERE id = :id")
    suspend fun updateRead(id: String, seq: Long, unread: Int)

    @Query("DELETE FROM conversations")
    suspend fun clear()
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY $NEWEST_FIRST LIMIT :limit")
    fun observeNewest(conversationId: String, limit: Int): Flow<List<MessageEntity>>

    /** The newest message of every conversation (pending ones included). */
    @Query(
        """
        SELECT * FROM messages WHERE localId IN (
            SELECT (SELECT m.localId FROM messages m WHERE m.conversationId = c.id ORDER BY (m.seq IS NULL) DESC, m.seq DESC, m.createdAt DESC LIMIT 1)
            FROM conversations c
        )
        """
    )
    fun observeLatestPerConversation(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE localId = :localId")
    suspend fun get(localId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE serverId = :serverId")
    suspend fun getByServerId(serverId: String): MessageEntity?

    @Query("SELECT MIN(seq) FROM messages WHERE conversationId = :conversationId AND seq IS NOT NULL")
    suspend fun oldestSeq(conversationId: String): Long?

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId AND mine = 0 AND seq > :afterSeq")
    suspend fun countFromPeerAfter(conversationId: String, afterSeq: Long): Int

    @Upsert
    suspend fun upsert(message: MessageEntity)

    @Query("DELETE FROM messages WHERE localId = :localId")
    suspend fun delete(localId: String)

    @Query("DELETE FROM messages")
    suspend fun clear()
}

@Dao
interface OutboxDao {
    @Query("SELECT * FROM outbox ORDER BY createdAt ASC, id ASC")
    suspend fun all(): List<OutboxEntity>

    @Query("SELECT COUNT(*) FROM outbox")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM outbox WHERE type = 'MARK_READ' AND conversationId = :conversationId LIMIT 1")
    suspend fun markReadFor(conversationId: String): OutboxEntity?

    @Query("SELECT COUNT(*) FROM outbox WHERE localId = :localId AND type IN ('EDIT', 'DELETE')")
    suspend fun countLocalChanges(localId: String): Int

    @Upsert
    suspend fun upsert(operation: OutboxEntity)

    @Query("DELETE FROM outbox WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM outbox WHERE localId = :localId")
    suspend fun deleteFor(localId: String)

    @Query("DELETE FROM outbox")
    suspend fun clear()
}

@Dao
interface SyncStateDao {
    @Query("SELECT cursor FROM sync_state WHERE id = 0")
    suspend fun cursor(): Long?

    @Upsert
    suspend fun upsert(state: SyncStateEntity)

    @Query("DELETE FROM sync_state")
    suspend fun clear()
}
