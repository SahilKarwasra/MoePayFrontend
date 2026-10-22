package com.laarasoft.moepay.auth.presentation.di

import com.laarasoft.moepay.auth.presentation.otp.OtpViewModel
import com.laarasoft.moepay.auth.presentation.phone.PhoneNumberViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authPresentationModule = module {
    viewModelOf(::PhoneNumberViewModel)
    viewModel { params ->
        OtpViewModel(
            phoneNumber = params.get(),
            resendAfterSeconds = params.get(),
            authService = get(),
            sessionStorage = get(),
        )
    }
}
