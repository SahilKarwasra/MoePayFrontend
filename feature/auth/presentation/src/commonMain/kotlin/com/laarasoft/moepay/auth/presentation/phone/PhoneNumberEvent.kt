package com.laarasoft.moepay.auth.presentation.phone

sealed interface PhoneNumberEvent {
    data class OtpSent(
        val phoneNumber: String,
        val resendAfterSeconds: Int,
    ) : PhoneNumberEvent
}
