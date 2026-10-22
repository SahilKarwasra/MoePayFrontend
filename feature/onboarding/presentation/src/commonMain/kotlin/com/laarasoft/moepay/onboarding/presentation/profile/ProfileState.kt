package com.laarasoft.moepay.onboarding.presentation.profile

import androidx.compose.foundation.text.input.TextFieldState
import com.laarasoft.moepay.core.presentation.util.UiText

data class ProfileState(
    val firstNameState: TextFieldState = TextFieldState(),
    val lastNameState: TextFieldState = TextFieldState(),
    val emailState: TextFieldState = TextFieldState(),
    val isEmailValid: Boolean = true,
    val canContinue: Boolean = false,
    val isSaving: Boolean = false,
    val error: UiText? = null,
)
