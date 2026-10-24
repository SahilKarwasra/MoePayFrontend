package com.laarasoft.moepay.wallet.presentation.pin

import androidx.compose.foundation.text.input.TextFieldState
import com.laarasoft.moepay.core.presentation.util.UiText

@kotlinx.serialization.Serializable
enum class PinMode {
    SET,
    CHANGE,
    RESET,
    ;

    /** The entry steps this flow walks through, in order (drives the progress indicator). */
    val steps: List<PinStep>
        get() = when (this) {
            SET -> listOf(PinStep.NEW, PinStep.CONFIRM)
            CHANGE -> listOf(PinStep.CURRENT, PinStep.NEW, PinStep.CONFIRM)
            RESET -> listOf(PinStep.OTP, PinStep.NEW, PinStep.CONFIRM)
        }
}

enum class PinStep {
    OTP,
    CURRENT,
    NEW,
    CONFIRM,
    DONE,
}

data class PinFlowState(
    val mode: PinMode,
    val step: PinStep,
    val error: UiText? = null,
    /** Bumped on every rejected entry so the pad clears and shakes. */
    val errorKey: Int = 0,
    val isLoading: Boolean = false,
    val otpState: TextFieldState = TextFieldState(),
    val otpPhone: String = "",
    val resendSecondsLeft: Int = 0,
    val isSendingOtp: Boolean = false,
    /** Set when PIN attempts are exhausted; offers the reset flow. */
    val lockedMessage: UiText? = null,
)

sealed interface PinFlowAction {
    data class OnPinEntered(val pin: String) : PinFlowAction
    data object OnResendOtp : PinFlowAction
    data object OnBack : PinFlowAction
    data object OnDone : PinFlowAction
    data object OnResetPinFromLock : PinFlowAction
    data object OnDismissLocked : PinFlowAction
}

sealed interface PinFlowEvent {
    data object Finished : PinFlowEvent
    data object OpenReset : PinFlowEvent
}
