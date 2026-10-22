package com.laarasoft.moepay.auth.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface AuthGraphRoutes {
    @Serializable
    data object Graph : AuthGraphRoutes

    @Serializable
    data object Welcome : AuthGraphRoutes

    @Serializable
    data object PhoneNumber : AuthGraphRoutes

    @Serializable
    data class OtpVerification(
        val phoneNumber: String,
        val resendAfterSeconds: Int,
    ) : AuthGraphRoutes
}
