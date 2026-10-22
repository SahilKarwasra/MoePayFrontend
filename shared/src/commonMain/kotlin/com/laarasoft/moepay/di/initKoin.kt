package com.laarasoft.moepay.di

import com.laarasoft.moepay.auth.presentation.di.authPresentationModule
import com.laarasoft.moepay.core.data.di.coreDataModule
import com.laarasoft.moepay.home.presentation.di.homePresentationModule
import com.laarasoft.moepay.onboarding.data.di.onboardingDataModule
import com.laarasoft.moepay.onboarding.presentation.di.onboardingPresentationModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            coreDataModule,
            appModule,
            authPresentationModule,
            onboardingDataModule,
            onboardingPresentationModule,
            homePresentationModule,
        )
    }
}
