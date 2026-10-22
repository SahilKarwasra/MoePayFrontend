package com.laarasoft.moepay.core.data.mappers

import com.laarasoft.moepay.core.data.dto.LoginResponseDto
import com.laarasoft.moepay.core.data.dto.SendOtpResponseDto
import com.laarasoft.moepay.core.data.dto.StoredSession
import com.laarasoft.moepay.core.data.dto.UserDto
import com.laarasoft.moepay.core.domain.auth.AuthInfo
import com.laarasoft.moepay.core.domain.auth.OtpChallenge
import com.laarasoft.moepay.core.domain.auth.User
import com.laarasoft.moepay.core.domain.onboarding.OnboardingStep

/** Backend step names: profile, username, review, completed. */
fun String.toOnboardingStep(): OnboardingStep = when (lowercase()) {
    "username" -> OnboardingStep.USERNAME
    "review" -> OnboardingStep.REVIEW
    "completed" -> OnboardingStep.COMPLETED
    else -> OnboardingStep.PROFILE
}

fun UserDto.toDomain(): User = User(
    id = id,
    phoneNumber = phoneNumber,
    username = username,
    firstName = profile.firstName,
    lastName = profile.lastName,
    email = profile.email,
)

internal fun SendOtpResponseDto.toDomain(): OtpChallenge = OtpChallenge(
    phoneNumber = phoneNumber,
    expiresInSeconds = expiresInSeconds,
    resendAfterSeconds = resendAfterSeconds,
)

internal fun LoginResponseDto.toDomain(): AuthInfo = AuthInfo(
    accessToken = accessToken,
    refreshToken = refreshToken,
    user = user.toDomain(),
    onboardingStep = onboarding.nextStep.toOnboardingStep(),
)

internal fun AuthInfo.toStored(): StoredSession = StoredSession(
    accessToken = accessToken,
    refreshToken = refreshToken,
    userId = user.id,
    phoneNumber = user.phoneNumber,
    username = user.username,
    firstName = user.firstName,
    lastName = user.lastName,
    email = user.email,
    onboardingStep = onboardingStep.name,
)

internal fun StoredSession.toDomain(): AuthInfo = AuthInfo(
    accessToken = accessToken,
    refreshToken = refreshToken,
    user = User(
        id = userId,
        phoneNumber = phoneNumber,
        username = username,
        firstName = firstName,
        lastName = lastName,
        email = email,
    ),
    onboardingStep = OnboardingStep.entries.firstOrNull { it.name == onboardingStep } ?: OnboardingStep.PROFILE,
)
