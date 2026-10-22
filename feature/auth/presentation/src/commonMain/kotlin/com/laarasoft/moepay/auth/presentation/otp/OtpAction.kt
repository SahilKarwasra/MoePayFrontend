package com.laarasoft.moepay.auth.presentation.otp

sealed interface OtpAction {
    data object OnVerifyClick : OtpAction
    data object OnResendClick : OtpAction
    data object OnBackClick : OtpAction
}
