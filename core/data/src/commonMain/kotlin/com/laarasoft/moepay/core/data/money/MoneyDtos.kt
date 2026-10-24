package com.laarasoft.moepay.core.data.money

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoneyDto(
    @SerialName("amount_pence") val amountPence: Long,
    val currency: String = "GBP",
    val display: String,
)

@Serializable
data class PartyDto(
    val kind: String = "user",
    @SerialName("display_name") val displayName: String = "",
    @SerialName("user_id") val userId: String? = null,
    val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("bank_name") val bankName: String? = null,
    @SerialName("account_last4") val accountLast4: String? = null,
)

@Serializable
data class FailureDto(
    val code: String,
    val reason: String,
)

@Serializable
data class TransactionDto(
    val id: String,
    val reference: String = "",
    val type: String,
    val status: String,
    val direction: String,
    val title: String,
    val amount: MoneyDto,
    @SerialName("signed_display") val signedDisplay: String,
    @SerialName("balance_after") val balanceAfter: MoneyDto? = null,
    val counterparty: PartyDto,
    val note: String? = null,
    val failure: FailureDto? = null,
    @SerialName("money_request_id") val moneyRequestId: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("completed_at") val completedAt: String? = null,
)

@Serializable
data class TransactionPageDto(
    val items: List<TransactionDto> = emptyList(),
    @SerialName("next_cursor") val nextCursor: String? = null,
    @SerialName("has_more") val hasMore: Boolean = false,
)

/** `{transaction, balance}`: the result of every money-moving call and of wallet events. */
@Serializable
data class MoneyMovementDto(
    val transaction: TransactionDto,
    val balance: MoneyDto,
)

/** Go embeds Recipient into RecentRecipient, so the fields arrive flattened. */
@Serializable
data class RecipientDto(
    @SerialName("user_id") val userId: String,
    val username: String? = null,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("phone_masked") val phoneMasked: String = "",
    @SerialName("last_paid_at") val lastPaidAt: String? = null,
)

@Serializable
data class MoneyRequestPartyDto(
    @SerialName("user_id") val userId: String,
    val username: String? = null,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

@Serializable
data class MoneyRequestDto(
    val id: String,
    val direction: String,
    val status: String,
    val amount: MoneyDto,
    val note: String? = null,
    val counterparty: MoneyRequestPartyDto,
    val actions: List<String> = emptyList(),
    @SerialName("paid_transaction_id") val paidTransactionId: String? = null,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("resolved_at") val resolvedAt: String? = null,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class MoneyRequestPageDto(
    val items: List<MoneyRequestDto> = emptyList(),
    @SerialName("next_cursor") val nextCursor: String? = null,
    @SerialName("has_more") val hasMore: Boolean = false,
)

@Serializable
data class RequestCountsDto(
    @SerialName("incoming_pending") val incomingPending: Int = 0,
    @SerialName("outgoing_pending") val outgoingPending: Int = 0,
)

@Serializable
data class PinStatusDto(
    val set: Boolean,
    val locked: Boolean = false,
    @SerialName("locked_until") val lockedUntil: String? = null,
    @SerialName("attempts_remaining") val attemptsRemaining: Int = 0,
)

@Serializable
data class WalletSummaryDto(
    val id: String,
    val status: String,
    val balance: MoneyDto,
    val pin: PinStatusDto,
)

@Serializable
data class ActionDto(
    val id: String,
    val label: String,
    val enabled: Boolean,
    @SerialName("disabled_reason") val disabledReason: String? = null,
)

@Serializable
data class BankAccountDto(
    val id: String,
    @SerialName("holder_name") val holderName: String,
    @SerialName("bank_name") val bankName: String,
    @SerialName("sort_code") val sortCode: String,
    @SerialName("account_number_masked") val accountNumberMasked: String,
    val last4: String,
    @SerialName("is_primary") val isPrimary: Boolean,
)

@Serializable
data class DailyLimitDto(
    val limit: MoneyDto,
    val used: MoneyDto,
    val remaining: MoneyDto,
    @SerialName("resets_at") val resetsAt: String,
)

@Serializable
data class LimitsDto(
    @SerialName("min_per_transaction") val minPerTransaction: MoneyDto,
    @SerialName("max_per_transaction") val maxPerTransaction: MoneyDto,
    @SerialName("max_balance") val maxBalance: MoneyDto,
    @SerialName("can_add") val canAdd: MoneyDto,
    @SerialName("daily_outgoing") val dailyOutgoing: DailyLimitDto,
)

@Serializable
data class WalletScreenDto(
    val wallet: WalletSummaryDto,
    val limits: LimitsDto,
    val actions: List<ActionDto> = emptyList(),
    @SerialName("bank_accounts") val bankAccounts: List<BankAccountDto> = emptyList(),
    val requests: RequestCountsDto = RequestCountsDto(),
    @SerialName("recent_transactions") val recentTransactions: List<TransactionDto> = emptyList(),
)
