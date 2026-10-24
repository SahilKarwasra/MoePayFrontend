package com.laarasoft.moepay.core.domain.money

import kotlin.time.Instant

enum class TransactionType {
    P2P_TRANSFER,
    ADD_MONEY,
    WITHDRAW,
    REQUEST_PAYMENT,
    UNKNOWN,
}

enum class TransactionStatus {
    SUCCEEDED,
    FAILED,
    UNKNOWN,
}

enum class TransactionDirection {
    IN,
    OUT,
}

enum class PartyKind {
    USER,
    BANK,
}

/** The other side of a transaction: a MoePay user or a bank account. */
data class Party(
    val kind: PartyKind,
    val displayName: String,
    val userId: String? = null,
    val username: String? = null,
    val avatarUrl: String? = null,
    val bankName: String? = null,
    val accountLast4: String? = null,
)

data class TransactionFailure(
    val code: String,
    val reason: String,
)

data class Transaction(
    val id: String,
    val reference: String,
    val type: TransactionType,
    val status: TransactionStatus,
    val direction: TransactionDirection,
    val title: String,
    val amount: Money,
    val signedDisplay: String,
    val balanceAfter: Money?,
    val counterparty: Party,
    val note: String?,
    val failure: TransactionFailure?,
    val moneyRequestId: String?,
    val createdAt: Instant,
    val completedAt: Instant?,
) {
    val isFailed: Boolean get() = status == TransactionStatus.FAILED
}

data class TransactionPage(
    val items: List<Transaction>,
    val nextCursor: String?,
    val hasMore: Boolean,
)

/** The outcome of any money-moving request: the new transaction and the wallet balance after it. */
data class MoneyMovement(
    val transaction: Transaction,
    val balance: Money,
)
