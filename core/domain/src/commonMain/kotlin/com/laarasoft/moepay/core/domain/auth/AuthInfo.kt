package com.laarasoft.moepay.core.domain.auth

import com.laarasoft.moepay.core.domain.onboarding.OnboardingStep

/** The signed-in session persisted on device. */
data class AuthInfo(
    val accessToken: String,
    val refreshToken: String,
    val user: User,
    val onboardingStep: OnboardingStep,
) {
    val isOnboarded: Boolean
        get() = onboardingStep == OnboardingStep.COMPLETED
}
