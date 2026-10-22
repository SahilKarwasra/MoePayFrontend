package com.laarasoft.moepay.onboarding.presentation.profile

sealed interface ProfileAction {
    data object OnContinueClick : ProfileAction
    data object OnBackClick : ProfileAction
}
