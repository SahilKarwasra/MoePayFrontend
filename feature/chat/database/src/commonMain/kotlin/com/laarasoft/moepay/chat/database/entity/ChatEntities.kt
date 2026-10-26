package com.laarasoft.moepay.chat.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val peerUserId: String,
    val peerUsername: String?,
    val peerDisplayName: String,
    val peerAvatarUrl: String?,
    val lastSeq: Long,
    /** Epoch milliseconds. */
    val lastMessageAt: Long,
    val unreadCount: Int,
    val myLastReadSeq: Long,
    val peerLastReadSeq: Long,
    /** Local only. */
    val draft: String = "",
)

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["conversationId", "seq"]),
        Index(value = ["serverId"], unique = true),
    ],
)
data class MessageEntity(
    @PrimaryKey val localId: String,
    val serverId: String?,
    val conversationId: String,
    val seq: Long?,
    val senderId: String,
    val mine: Boolean,
    /** "text", "payment" or "request". */
    val kind: String,
    val body: String?,
    val replyToId: String?,
    // Payment and request messages
    val moneyId: String?,
    val reference: String?,
    val amountPence: Long?,
    val amountDisplay: String?,
    val outgoing: Boolean?,
    val moneyStatus: String?,
    /** Comma-separated request actions. */
    val actions: String?,
    val expiresAt: Long?,
    val version: Int,
    val edited: Boolean,
    val deleted: Boolean,
    /** Epoch milliseconds. */
    val createdAt: Long,
    /** "PENDING", "SENT" or "FAILED". */
    val delivery: String,
    /** A `DataError.Remote` name when delivery failed. */
    val failure: String? = null,
)

@Entity(tableName = "outbox", indices = [Index(value = ["conversationId"]), Index(value = ["localId"])])
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val conversationId: String,
    val localId: String?,
    val body: String?,
    val replyToId: String?,
    val baseVersion: Int?,
    val amountPence: Long?,
    val upToSeq: Long?,
    val attempts: Int,
    val nextAttemptAt: Long,
    val createdAt: Long,
)

@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey val id: Int = 0,
    val cursor: Long,
)
