package com.laarasoft.moepay.core.presentation.util

import com.laarasoft.moepay.core.domain.wallet.WalletError
import com.laarasoft.moepay.core.presentation.resources.Res
import com.laarasoft.moepay.core.presentation.resources.error_bank_declined
import com.laarasoft.moepay.core.presentation.resources.error_bank_declined_reason
import com.laarasoft.moepay.core.presentation.resources.error_pin_invalid
import com.laarasoft.moepay.core.presentation.resources.error_pin_invalid_attempts
import com.laarasoft.moepay.core.presentation.resources.error_pin_invalid_last_attempt
import com.laarasoft.moepay.core.presentation.resources.error_pin_locked
import com.laarasoft.moepay.core.presentation.resources.error_pin_locked_until

fun WalletError.toUiText(): UiText = when (this) {
    is WalletError.PinInvalid -> when (val left = attemptsRemaining) {
        null -> UiText.Resource(Res.string.error_pin_invalid)
        1 -> UiText.Resource(Res.string.error_pin_invalid_last_attempt)
        else -> UiText.Resource(Res.string.error_pin_invalid_attempts, arrayOf(left))
    }
    is WalletError.PinLocked -> lockedUntil
        ?.let { UiText.Resource(Res.string.error_pin_locked_until, arrayOf(it.formatTime())) }
        ?: UiText.Resource(Res.string.error_pin_locked)
    is WalletError.BankDeclined -> reason
        ?.let { UiText.Resource(Res.string.error_bank_declined_reason, arrayOf(it)) }
        ?: UiText.Resource(Res.string.error_bank_declined)
    is WalletError.Remote -> error.toUiText()
}
