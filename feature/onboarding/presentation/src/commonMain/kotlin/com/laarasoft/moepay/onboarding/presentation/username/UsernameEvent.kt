package com.laarasoft.moepay.onboarding.presentation.username

import com.laarasoft.moepay.core.domain.onboarding.OnboardingStep

sealed interface UsernameEvent {
    data class NextStep(val step: OnboardingStep) : UsernameEvent
}
