package com.laarasoft.moepay.onboarding.data.dto

import com.laarasoft.moepay.core.data.dto.ProfileDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OnboardingStateDto(
    val status: String,
    @SerialName("next_step") val nextStep: String,
    val data: OnboardingDataDto,
)

@Serializable
internal data class OnboardingDataDto(
    @SerialName("phone_number") val phoneNumber: String,
    val username: String? = null,
    val profile: ProfileDto = ProfileDto(),
)

@Serializable
internal data class SaveProfileRequest(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val email: String? = null,
)

@Serializable
internal data class SaveUsernameRequest(
    val username: String,
)

@Serializable
internal data class UsernameAvailabilityDto(
    val username: String,
    val available: Boolean,
    val reason: String,
)
