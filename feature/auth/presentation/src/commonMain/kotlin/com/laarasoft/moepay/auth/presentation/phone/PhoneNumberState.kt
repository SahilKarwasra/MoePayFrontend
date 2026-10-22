package com.laarasoft.moepay.auth.presentation.phone

import androidx.compose.foundation.text.input.TextFieldState
import com.laarasoft.moepay.core.presentation.util.UiText

data class PhoneNumberState(
    val phoneNumberState: TextFieldState = TextFieldState(),
    val canContinue: Boolean = false,
    val isSendingOtp: Boolean = false,
    val error: UiText? = null,
)
