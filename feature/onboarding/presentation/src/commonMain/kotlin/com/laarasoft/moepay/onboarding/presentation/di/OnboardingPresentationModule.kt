package com.laarasoft.moepay.onboarding.presentation.di

import com.laarasoft.moepay.onboarding.presentation.profile.ProfileViewModel
import com.laarasoft.moepay.onboarding.presentation.review.ReviewViewModel
import com.laarasoft.moepay.onboarding.presentation.username.UsernameViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingPresentationModule = module {
    viewModelOf(::ProfileViewModel)
    viewModelOf(::UsernameViewModel)
    viewModelOf(::ReviewViewModel)
}
