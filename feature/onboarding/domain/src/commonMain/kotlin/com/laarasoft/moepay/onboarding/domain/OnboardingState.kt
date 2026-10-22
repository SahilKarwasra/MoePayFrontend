package com.laarasoft.moepay.onboarding.domain

import com.laarasoft.moepay.core.domain.onboarding.OnboardingStep

data class OnboardingState(
    val nextStep: OnboardingStep,
    val phoneNumber: String,
    val username: String?,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
)

data class ProfileInput(
    val firstName: String,
    val lastName: String,
    val email: String?,
)

enum class UsernameAvailability {
    AVAILABLE,
    TAKEN,
    RESERVED,
    INVALID,
}
