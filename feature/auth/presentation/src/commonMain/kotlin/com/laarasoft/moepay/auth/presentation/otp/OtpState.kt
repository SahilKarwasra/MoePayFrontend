package com.laarasoft.moepay.auth.presentation.otp

import androidx.compose.foundation.text.input.TextFieldState
import com.laarasoft.moepay.core.presentation.util.UiText

data class OtpState(
    val otpState: TextFieldState = TextFieldState(),
    val formattedPhoneNumber: String = "",
    val isVerifying: Boolean = false,
    val canVerify: Boolean = false,
    val resendSecondsLeft: Int = 0,
    val isResending: Boolean = false,
    val error: UiText? = null,
    val info: UiText? = null,
) {
    val canResend: Boolean
        get() = resendSecondsLeft == 0 && !isResending && !isVerifying
}
