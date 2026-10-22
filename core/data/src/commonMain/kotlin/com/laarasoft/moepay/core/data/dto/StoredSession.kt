package com.laarasoft.moepay.core.data.dto

import kotlinx.serialization.Serializable

/** On-device representation of [com.laarasoft.moepay.core.domain.auth.AuthInfo]. */
@Serializable
internal data class StoredSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val phoneNumber: String,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val onboardingStep: String,
)
