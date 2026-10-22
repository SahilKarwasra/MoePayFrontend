package com.laarasoft.moepay.auth.presentation.otp

import com.laarasoft.moepay.core.domain.onboarding.OnboardingStep

sealed interface OtpEvent {
    data class Verified(val nextStep: OnboardingStep) : OtpEvent
}
