package com.laarasoft.moepay.onboarding.presentation.profile

import com.laarasoft.moepay.core.domain.onboarding.OnboardingStep

sealed interface ProfileEvent {
    data class NextStep(val step: OnboardingStep) : ProfileEvent
}
