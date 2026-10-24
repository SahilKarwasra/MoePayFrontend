package com.laarasoft.moepay.core.data.money

import com.laarasoft.moepay.core.domain.money.Money
import com.laarasoft.moepay.core.domain.money.MoneyMovement
import com.laarasoft.moepay.core.domain.money.MoneyRequest
import com.laarasoft.moepay.core.domain.money.MoneyRequestAction
import com.laarasoft.moepay.core.domain.money.MoneyRequestDirection
import com.laarasoft.moepay.core.domain.money.MoneyRequestPage
import com.laarasoft.moepay.core.domain.money.MoneyRequestParty
import com.laarasoft.moepay.core.domain.money.MoneyRequestStatus
import com.laarasoft.moepay.core.domain.money.Party
import com.laarasoft.moepay.core.domain.money.PartyKind
import com.laarasoft.moepay.core.domain.money.RecentRecipient
import com.laarasoft.moepay.core.domain.money.Recipient
import com.laarasoft.moepay.core.domain.money.RecipientRef
import com.laarasoft.moepay.core.domain.money.RequestCounts
import com.laarasoft.moepay.core.domain.money.Transaction
import com.laarasoft.moepay.core.domain.money.TransactionDirection
import com.laarasoft.moepay.core.domain.money.TransactionFailure
import com.laarasoft.moepay.core.domain.money.TransactionPage
import com.laarasoft.moepay.core.domain.money.TransactionStatus
import com.laarasoft.moepay.core.domain.money.TransactionType
import com.laarasoft.moepay.core.domain.wallet.ActionBlocker
import com.laarasoft.moepay.core.domain.wallet.BankAccount
import com.laarasoft.moepay.core.domain.wallet.DailyLimit
import com.laarasoft.moepay.core.domain.wallet.PinStatus
import com.laarasoft.moepay.core.domain.wallet.WalletAction
import com.laarasoft.moepay.core.domain.wallet.WalletActionId
import com.laarasoft.moepay.core.domain.wallet.WalletLimits
import com.laarasoft.moepay.core.domain.wallet.WalletOverview
import com.laarasoft.moepay.core.domain.wallet.WalletSummary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

fun MoneyDto.toDomain(): Money = Money(pence = amountPence, display = display)

fun String.toInstant(): Instant = Instant.parse(this)

fun String?.toInstantOrNull(): Instant? = this?.let { runCatching { Instant.parse(it) }.getOrNull() }

fun PartyDto.toDomain(): Party = Party(
    kind = if (kind == "bank") PartyKind.BANK else PartyKind.USER,
    displayName = displayName,
    userId = userId,
    username = username,
    avatarUrl = avatarUrl,
    bankName = bankName,
    accountLast4 = accountLast4,
)

fun TransactionDto.toDomain(): Transaction = Transaction(
    id = id,
    reference = reference,
    type = when (type) {
        "p2p_transfer" -> TransactionType.P2P_TRANSFER
        "add_money" -> TransactionType.ADD_MONEY
        "withdraw" -> TransactionType.WITHDRAW
        "request_payment" -> TransactionType.REQUEST_PAYMENT
        else -> TransactionType.UNKNOWN
    },
    status = when (status) {
        "succeeded" -> TransactionStatus.SUCCEEDED
        "failed" -> TransactionStatus.FAILED
        else -> TransactionStatus.UNKNOWN
    },
    direction = if (direction == "in") TransactionDirection.IN else TransactionDirection.OUT,
    title = title,
    amount = amount.toDomain(),
    signedDisplay = signedDisplay,
    balanceAfter = balanceAfter?.toDomain(),
    counterparty = counterparty.toDomain(),
    note = note,
    failure = failure?.let { TransactionFailure(it.code, it.reason) },
    moneyRequestId = moneyRequestId,
    createdAt = createdAt.toInstant(),
    completedAt = completedAt.toInstantOrNull(),
)

fun TransactionPageDto.toDomain(): TransactionPage =
    TransactionPage(items = items.map { it.toDomain() }, nextCursor = nextCursor, hasMore = hasMore)

fun MoneyMovementDto.toDomain(): MoneyMovement =
    MoneyMovement(transaction = transaction.toDomain(), balance = balance.toDomain())

fun RecipientDto.toDomain(): Recipient = Recipient(
    userId = userId,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
    phoneMasked = phoneMasked,
)

fun RecipientDto.toRecentRecipient(): RecentRecipient =
    RecentRecipient(recipient = toDomain(), lastPaidAt = lastPaidAt.toInstantOrNull() ?: Instant.DISTANT_PAST)

fun MoneyRequestDto.toDomain(): MoneyRequest = MoneyRequest(
    id = id,
    direction = if (direction == "incoming") MoneyRequestDirection.INCOMING else MoneyRequestDirection.OUTGOING,
    status = when (status) {
        "pending" -> MoneyRequestStatus.PENDING
        "paid" -> MoneyRequestStatus.PAID
        "declined" -> MoneyRequestStatus.DECLINED
        "cancelled" -> MoneyRequestStatus.CANCELLED
        "expired" -> MoneyRequestStatus.EXPIRED
        else -> MoneyRequestStatus.UNKNOWN
    },
    amount = amount.toDomain(),
    note = note,
    counterparty = MoneyRequestParty(
        userId = counterparty.userId,
        username = counterparty.username,
        displayName = counterparty.displayName,
        avatarUrl = counterparty.avatarUrl,
    ),
    actions = actions.mapNotNull {
        when (it) {
            "pay" -> MoneyRequestAction.PAY
            "decline" -> MoneyRequestAction.DECLINE
            "cancel" -> MoneyRequestAction.CANCEL
            else -> null
        }
    }.toSet(),
    paidTransactionId = paidTransactionId,
    expiresAt = expiresAt.toInstant(),
    resolvedAt = resolvedAt.toInstantOrNull(),
    createdAt = createdAt.toInstant(),
)

fun MoneyRequestPageDto.toDomain(): MoneyRequestPage =
    MoneyRequestPage(items = items.map { it.toDomain() }, nextCursor = nextCursor, hasMore = hasMore)

fun RequestCountsDto.toDomain(): RequestCounts = RequestCounts(incomingPending, outgoingPending)

fun PinStatusDto.toDomain(): PinStatus = PinStatus(
    isSet = set,
    isLocked = locked,
    lockedUntil = lockedUntil.toInstantOrNull(),
    attemptsRemaining = attemptsRemaining,
)

fun WalletSummaryDto.toDomain(): WalletSummary = WalletSummary(
    id = id,
    isActive = status == "active",
    balance = balance.toDomain(),
    pin = pin.toDomain(),
)

fun ActionDto.toDomain(): WalletAction = WalletAction(
    id = when (id) {
        "send" -> WalletActionId.SEND
        "request" -> WalletActionId.REQUEST
        "scan_qr" -> WalletActionId.SCAN_QR
        "add_money" -> WalletActionId.ADD_MONEY
        "withdraw" -> WalletActionId.WITHDRAW
        "history" -> WalletActionId.HISTORY
        else -> WalletActionId.UNKNOWN
    },
    label = label,
    blocker = if (enabled) null else when (disabledReason) {
        "WALLET_NOT_READY" -> ActionBlocker.WALLET_NOT_READY
        "PIN_NOT_SET" -> ActionBlocker.PIN_NOT_SET
        "NO_BANK_ACCOUNT" -> ActionBlocker.NO_BANK_ACCOUNT
        "INSUFFICIENT_BALANCE" -> ActionBlocker.INSUFFICIENT_BALANCE
        "WALLET_FROZEN" -> ActionBlocker.WALLET_FROZEN
        else -> ActionBlocker.UNKNOWN
    },
)

fun BankAccountDto.toDomain(): BankAccount = BankAccount(
    id = id,
    holderName = holderName,
    bankName = bankName,
    sortCode = sortCode,
    accountNumberMasked = accountNumberMasked,
    last4 = last4,
    isPrimary = isPrimary,
)

fun LimitsDto.toDomain(): WalletLimits = WalletLimits(
    minPerTransaction = minPerTransaction.toDomain(),
    maxPerTransaction = maxPerTransaction.toDomain(),
    maxBalance = maxBalance.toDomain(),
    canAdd = canAdd.toDomain(),
    dailyOutgoing = DailyLimit(
        limit = dailyOutgoing.limit.toDomain(),
        used = dailyOutgoing.used.toDomain(),
        remaining = dailyOutgoing.remaining.toDomain(),
        resetsAt = dailyOutgoing.resetsAt.toInstant(),
    ),
)

fun WalletScreenDto.toDomain(): WalletOverview = WalletOverview(
    wallet = wallet.toDomain(),
    limits = limits.toDomain(),
    actions = actions.map { it.toDomain() },
    bankAccounts = bankAccounts.map { it.toDomain() },
    requests = requests.toDomain(),
    recentTransactions = recentTransactions.map { it.toDomain() },
)

/** Wire form of a [RecipientRef]: exactly one field set. */
@Serializable
data class RecipientRefDto(
    @SerialName("user_id") val userId: String? = null,
    val username: String? = null,
    val phone: String? = null,
)

fun RecipientRef.toDto(): RecipientRefDto = when (this) {
    is RecipientRef.ByUserId -> RecipientRefDto(userId = userId)
    is RecipientRef.ByUsername -> RecipientRefDto(username = username)
    is RecipientRef.ByPhone -> RecipientRefDto(phone = phone)
}

/** Query parameter for `GET /recipients/resolve`. */
fun RecipientRef.toQuery(): Pair<String, String> = when (this) {
    is RecipientRef.ByUserId -> "user_id" to userId
    is RecipientRef.ByUsername -> "username" to username
    is RecipientRef.ByPhone -> "phone" to phone
}
