package com.laarasoft.moepay.core.domain.money

import kotlin.time.Instant

enum class MoneyRequestDirection {
    /** Someone is asking you to pay. */
    INCOMING,

    /** You asked someone to pay you. */
    OUTGOING,
}

enum class MoneyRequestStatus {
    PENDING,
    PAID,
    DECLINED,
    CANCELLED,
    EXPIRED,
    UNKNOWN,
}

enum class MoneyRequestAction {
    PAY,
    DECLINE,
    CANCEL,
}

data class MoneyRequestParty(
    val userId: String,
    val username: String?,
    val displayName: String,
    val avatarUrl: String?,
)

data class MoneyRequest(
    val id: String,
    val direction: MoneyRequestDirection,
    val status: MoneyRequestStatus,
    val amount: Money,
    val note: String?,
    val counterparty: MoneyRequestParty,
    /** Server-decided: what the viewer may do right now. */
    val actions: Set<MoneyRequestAction>,
    val paidTransactionId: String?,
    val expiresAt: Instant,
    val resolvedAt: Instant?,
    val createdAt: Instant,
)

data class MoneyRequestPage(
    val items: List<MoneyRequest>,
    val nextCursor: String?,
    val hasMore: Boolean,
)

data class RequestCounts(
    val incomingPending: Int,
    val outgoingPending: Int,
) {
    companion object {
        val None = RequestCounts(0, 0)
    }
}
