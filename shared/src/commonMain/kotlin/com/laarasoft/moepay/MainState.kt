package com.laarasoft.moepay

import com.laarasoft.moepay.core.domain.onboarding.OnboardingStep

/** Where the app opens, decided from the stored session while the splash is showing. */
sealed interface AppStart {
    data object Auth : AppStart
    data class Onboarding(val step: OnboardingStep) : AppStart
    data object Home : AppStart
}

data class MainState(
    val isReady: Boolean = false,
    val start: AppStart = AppStart.Auth,
)
