package com.laarasoft.moepay.onboarding.presentation.username

sealed interface UsernameAction {
    data object OnContinueClick : UsernameAction
    data object OnBackClick : UsernameAction
}
