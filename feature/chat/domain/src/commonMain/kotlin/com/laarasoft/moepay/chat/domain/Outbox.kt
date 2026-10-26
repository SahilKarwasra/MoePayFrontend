package com.laarasoft.moepay.chat.domain

import kotlin.time.Instant

enum class OutboxType {
    SEND_TEXT,
    SEND_REQUEST,
    EDIT,
    DELETE,
    MARK_READ,
}

/**
 * A change made on the device that still has to reach the server. Operations for one
 * conversation are sent in the order they were made.
 */
data class OutboxOperation(
    val id: Long = 0,
    val type: OutboxType,
    val conversationId: String,
    /** The message the operation is about (not used by [OutboxType.MARK_READ]). */
    val localId: String? = null,
    val body: String? = null,
    val replyToId: String? = null,
    val baseVersion: Int? = null,
    val amountPence: Long? = null,
    val upToSeq: Long? = null,
    val attempts: Int = 0,
    val nextAttemptAt: Instant,
    val createdAt: Instant,
)
