package com.laarasoft.moepay.core.domain.wallet

import com.laarasoft.moepay.core.domain.money.Money
import com.laarasoft.moepay.core.domain.money.RequestCounts
import com.laarasoft.moepay.core.domain.money.Transaction
import kotlin.time.Instant

data class PinStatus(
    val isSet: Boolean,
    val isLocked: Boolean,
    val lockedUntil: Instant?,
    val attemptsRemaining: Int,
)

data class WalletSummary(
    val id: String,
    val isActive: Boolean,
    val balance: Money,
    val pin: PinStatus,
)

data class DailyLimit(
    val limit: Money,
    val used: Money,
    val remaining: Money,
    val resetsAt: Instant,
)

data class WalletLimits(
    val minPerTransaction: Money,
    val maxPerTransaction: Money,
    val maxBalance: Money,
    /** How much can still be added before the wallet hits [maxBalance]. */
    val canAdd: Money,
    val dailyOutgoing: DailyLimit,
)

data class BankAccount(
    val id: String,
    val holderName: String,
    val bankName: String,
    val sortCode: String,
    val accountNumberMasked: String,
    val last4: String,
    val isPrimary: Boolean,
)

enum class WalletActionId {
    SEND,
    REQUEST,
    SCAN_QR,
    ADD_MONEY,
    WITHDRAW,
    HISTORY,
    UNKNOWN,
}

/** Why the server disabled an action; each maps to the screen that fixes it. */
enum class ActionBlocker {
    WALLET_NOT_READY,
    PIN_NOT_SET,
    NO_BANK_ACCOUNT,
    INSUFFICIENT_BALANCE,
    WALLET_FROZEN,
    UNKNOWN,
}

data class WalletAction(
    val id: WalletActionId,
    val label: String,
    val blocker: ActionBlocker?,
) {
    val isEnabled: Boolean get() = blocker == null
}

/** Everything on the eWallet screen, from `GET /wallet`. */
data class WalletOverview(
    val wallet: WalletSummary,
    val limits: WalletLimits,
    val actions: List<WalletAction>,
    val bankAccounts: List<BankAccount>,
    val requests: RequestCounts,
    val recentTransactions: List<Transaction>,
) {
    val primaryBankAccount: BankAccount? get() = bankAccounts.firstOrNull { it.isPrimary } ?: bankAccounts.firstOrNull()
}
