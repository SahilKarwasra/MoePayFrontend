package com.laarasoft.moepay.chat.presentation.money

import androidx.compose.foundation.text.input.TextFieldState
import com.laarasoft.moepay.core.domain.wallet.WalletOverview
import com.laarasoft.moepay.core.presentation.money.AmountInput
import com.laarasoft.moepay.core.presentation.money.AmountKey
import com.laarasoft.moepay.core.presentation.util.UiText

enum class ChatMoneyMode {
    SEND,
    REQUEST,
}

data class ChatMoneyState(
    /** Null while the sheet is closed. */
    val mode: ChatMoneyMode? = null,
    val peerName: String = "",
    val amount: AmountInput = AmountInput(),
    val rejectKey: Int = 0,
    val noteState: TextFieldState = TextFieldState(),
    val overview: WalletOverview? = null,
    val isPinVisible: Boolean = false,
    val pinError: UiText? = null,
    val pinErrorKey: Int = 0,
    val isSubmitting: Boolean = false,
    val lockedMessage: UiText? = null,
    val error: UiText? = null,
) {
    /** Per-payment cap; sends are also capped by the balance and today's allowance. */
    val maxPence: Long
        get() {
            val o = overview ?: return Long.MAX_VALUE
            return when (mode) {
                ChatMoneyMode.SEND -> minOf(o.limits.maxPerTransaction.pence, o.wallet.balance.pence, o.limits.dailyOutgoing.remaining.pence)
                else -> o.limits.maxPerTransaction.pence
            }
        }

    val minPence: Long get() = overview?.limits?.minPerTransaction?.pence ?: 1

    val canContinue: Boolean get() = amount.pence in minPence..maxPence
}

sealed interface ChatMoneyAction {
    data class OnOpen(val mode: ChatMoneyMode) : ChatMoneyAction
    data object OnDismiss : ChatMoneyAction
    data class OnKey(val key: AmountKey) : ChatMoneyAction
    data object OnContinue : ChatMoneyAction
    data class OnPinEntered(val pin: String) : ChatMoneyAction
    data object OnDismissPin : ChatMoneyAction
    data object OnForgotPin : ChatMoneyAction
    data object OnDismissLocked : ChatMoneyAction
    data object OnDismissError : ChatMoneyAction
}

sealed interface ChatMoneyEvent {
    data object Done : ChatMoneyEvent
    data object OpenResetPin : ChatMoneyEvent
}
