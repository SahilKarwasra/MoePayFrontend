package com.laarasoft.moepay.core.domain.auth

data class OtpChallenge(
    val phoneNumber: String,
    val expiresInSeconds: Int,
    val resendAfterSeconds: Int,
)
