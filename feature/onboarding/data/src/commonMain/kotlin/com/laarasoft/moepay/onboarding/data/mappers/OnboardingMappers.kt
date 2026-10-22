package com.laarasoft.moepay.onboarding.data.mappers

import com.laarasoft.moepay.core.data.mappers.toOnboardingStep
import com.laarasoft.moepay.onboarding.data.dto.OnboardingStateDto
import com.laarasoft.moepay.onboarding.data.dto.UsernameAvailabilityDto
import com.laarasoft.moepay.onboarding.domain.OnboardingState
import com.laarasoft.moepay.onboarding.domain.UsernameAvailability

internal fun OnboardingStateDto.toDomain(): OnboardingState = OnboardingState(
    nextStep = nextStep.toOnboardingStep(),
    phoneNumber = data.phoneNumber,
    username = data.username,
    firstName = data.profile.firstName,
    lastName = data.profile.lastName,
    email = data.profile.email,
)

/** Backend reasons: available, taken, reserved, invalid. */
internal fun UsernameAvailabilityDto.toDomain(): UsernameAvailability = when {
    available -> UsernameAvailability.AVAILABLE
    reason == "reserved" -> UsernameAvailability.RESERVED
    reason == "invalid" -> UsernameAvailability.INVALID
    else -> UsernameAvailability.TAKEN
}
