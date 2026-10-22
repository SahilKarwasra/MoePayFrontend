package com.laarasoft.moepay.onboarding.presentation.review

sealed interface ReviewAction {
    data object OnFinishClick : ReviewAction
    data object OnBackClick : ReviewAction
}
