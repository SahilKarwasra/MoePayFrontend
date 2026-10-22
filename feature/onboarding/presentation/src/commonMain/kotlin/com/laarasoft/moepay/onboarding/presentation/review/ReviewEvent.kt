package com.laarasoft.moepay.onboarding.presentation.review

sealed interface ReviewEvent {
    data object Completed : ReviewEvent
}
