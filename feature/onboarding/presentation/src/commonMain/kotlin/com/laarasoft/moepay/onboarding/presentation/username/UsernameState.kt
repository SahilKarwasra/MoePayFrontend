package com.laarasoft.moepay.onboarding.presentation.username

import androidx.compose.foundation.text.input.TextFieldState
import com.laarasoft.moepay.core.presentation.util.UiText

enum class UsernameStatus {
    IDLE,
    CHECKING,
    AVAILABLE,
    TAKEN,
    RESERVED,
    INVALID,
}

data class UsernameState(
    val usernameState: TextFieldState = TextFieldState(),
    val status: UsernameStatus = UsernameStatus.IDLE,
    val isSaving: Boolean = false,
    val error: UiText? = null,
) {
    val canContinue: Boolean
        get() = status == UsernameStatus.AVAILABLE && !isSaving
}
