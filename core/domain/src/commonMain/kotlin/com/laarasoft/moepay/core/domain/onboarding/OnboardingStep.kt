package com.laarasoft.moepay.core.domain.onboarding

/** Mirrors the backend onboarding state machine: profile -> username -> review -> completed. */
enum class OnboardingStep {
    PROFILE,
    USERNAME,
    REVIEW,
    COMPLETED,
}
