package com.laarasoft.moepay.onboarding.data.di

import com.laarasoft.moepay.onboarding.data.KtorOnboardingService
import com.laarasoft.moepay.onboarding.domain.OnboardingService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val onboardingDataModule = module {
    singleOf(::KtorOnboardingService) bind OnboardingService::class
}
