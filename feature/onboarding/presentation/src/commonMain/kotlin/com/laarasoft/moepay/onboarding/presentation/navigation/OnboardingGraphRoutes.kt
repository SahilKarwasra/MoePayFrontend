package com.laarasoft.moepay.onboarding.presentation.navigation

import com.laarasoft.moepay.core.domain.onboarding.OnboardingStep
import kotlinx.serialization.Serializable

sealed interface OnboardingGraphRoutes {
    @Serializable
    data object Graph : OnboardingGraphRoutes

    @Serializable
    data object Profile : OnboardingGraphRoutes

    @Serializable
    data object Username : OnboardingGraphRoutes

    @Serializable
    data object Review : OnboardingGraphRoutes
}

/** The screen for a backend step. COMPLETED has no screen; the caller leaves onboarding. */
fun OnboardingStep.toRoute(): OnboardingGraphRoutes = when (this) {
    OnboardingStep.PROFILE -> OnboardingGraphRoutes.Profile
    OnboardingStep.USERNAME -> OnboardingGraphRoutes.Username
    OnboardingStep.REVIEW,
    OnboardingStep.COMPLETED -> OnboardingGraphRoutes.Review
}

internal const val ONBOARDING_STEP_COUNT = 3
