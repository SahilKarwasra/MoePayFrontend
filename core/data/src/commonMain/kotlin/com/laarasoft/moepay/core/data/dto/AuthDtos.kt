package com.laarasoft.moepay.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendOtpRequest(
    @SerialName("phone_number") val phoneNumber: String,
)

@Serializable
data class SendOtpResponseDto(
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("expires_in_seconds") val expiresInSeconds: Int,
    @SerialName("resend_after_seconds") val resendAfterSeconds: Int,
)

@Serializable
data class VerifyOtpRequest(
    @SerialName("phone_number") val phoneNumber: String,
    val otp: String,
)

@Serializable
data class RefreshRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class TokenPairDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class LoginResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("is_user_new") val isUserNew: Boolean = false,
    val user: UserDto,
    val onboarding: OnboardingSummaryDto,
)

@Serializable
data class OnboardingSummaryDto(
    val status: String,
    @SerialName("next_step") val nextStep: String,
)
