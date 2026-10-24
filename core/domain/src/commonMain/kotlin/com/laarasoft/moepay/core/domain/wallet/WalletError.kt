package com.laarasoft.moepay.core.domain.wallet

import com.laarasoft.moepay.core.domain.money.Transaction
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Error
import kotlin.time.Instant

/** Failures of PIN-protected wallet calls, keeping the details the UI needs to react. */
sealed interface WalletError : Error {
    data class PinInvalid(val attemptsRemaining: Int?) : WalletError
    data class PinLocked(val lockedUntil: Instant?) : WalletError

    /** The bank refused; a failed transaction was recorded and is in history. */
    data class BankDeclined(val reason: String?, val transaction: Transaction?) : WalletError
    data class Remote(val error: DataError.Remote) : WalletError
}
