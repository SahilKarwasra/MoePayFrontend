package com.laarasoft.moepay.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    @SerialName("phone_number") val phoneNumber: String,
    val username: String? = null,
    val profile: ProfileDto = ProfileDto(),
)

@Serializable
data class ProfileDto(
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val email: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)
