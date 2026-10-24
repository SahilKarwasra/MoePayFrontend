package com.laarasoft.moepay.payments.presentation.transfer

import androidx.compose.foundation.text.input.TextFieldState
import com.laarasoft.moepay.core.domain.money.MoneyMovement
import com.laarasoft.moepay.core.domain.money.MoneyRequest
import com.laarasoft.moepay.core.domain.money.RecentRecipient
import com.laarasoft.moepay.core.domain.money.Recipient
import com.laarasoft.moepay.core.domain.wallet.WalletOverview
import com.laarasoft.moepay.core.presentation.money.AmountInput
import com.laarasoft.moepay.core.presentation.money.AmountKey
import com.laarasoft.moepay.core.presentation.util.UiText
import kotlinx.serialization.Serializable

@Serializable
enum class TransferMode {
    SEND,
    REQUEST,
}

enum class TransferStep {
    RECIPIENT,
    AMOUNT,
}

sealed interface TransferResult {
    data class Sent(val movement: MoneyMovement, val recipient: Recipient) : TransferResult
    data class Requested(val request: MoneyRequest) : TransferResult
}

data class TransferFlowState(
    val mode: TransferMode,
    val step: TransferStep = TransferStep.RECIPIENT,
    /** True when the flow started with a recipient (from Home or a QR); back then leaves. */
    val recipientFixed: Boolean = false,
    val queryState: TextFieldState = TextFieldState(),
    val isResolving: Boolean = false,
    val searchResult: Recipient? = null,
    val searchError: UiText? = null,
    val recents: List<RecentRecipient> = emptyList(),
    val isLoadingQr: Boolean = false,
    val recipient: Recipient? = null,
    val amount: AmountInput = AmountInput(),
    val amountLocked: Boolean = false,
    val rejectKey: Int = 0,
    val noteState: TextFieldState = TextFieldState(),
    val overview: WalletOverview? = null,
    val submitError: UiText? = null,
    val isReviewVisible: Boolean = false,
    val isPinVisible: Boolean = false,
    val pinError: UiText? = null,
    val pinErrorKey: Int = 0,
    val isSubmitting: Boolean = false,
    val lockedMessage: UiText? = null,
    val result: TransferResult? = null,
)

sealed interface TransferFlowAction {
    data class OnRecipientSelected(val recipient: Recipient) : TransferFlowAction
    data class OnKey(val key: AmountKey) : TransferFlowAction
    data object OnContinue : TransferFlowAction
    data object OnDismissReview : TransferFlowAction
    data object OnConfirm : TransferFlowAction
    data class OnPinEntered(val pin: String) : TransferFlowAction
    data object OnDismissPin : TransferFlowAction
    data object OnDismissLocked : TransferFlowAction
    data object OnBack : TransferFlowAction
    data object OnScanClick : TransferFlowAction
    data object OnDone : TransferFlowAction
    data object OnForgotPin : TransferFlowAction
}

sealed interface TransferFlowEvent {
    data object Close : TransferFlowEvent
    data object OpenScanner : TransferFlowEvent
    data object OpenResetPin : TransferFlowEvent
}

/** The most this payment may be: per-payment cap, plus balance and today's allowance for sends. */
internal fun TransferFlowState.maxPence(): Long {
    val o = overview ?: return Long.MAX_VALUE
    return when (mode) {
        TransferMode.SEND -> minOf(o.limits.maxPerTransaction.pence, o.wallet.balance.pence, o.limits.dailyOutgoing.remaining.pence)
        TransferMode.REQUEST -> o.limits.maxPerTransaction.pence
    }
}

internal fun TransferFlowState.minPence(): Long = overview?.limits?.minPerTransaction?.pence ?: 1
