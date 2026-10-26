package com.laarasoft.moepay.di

import com.laarasoft.moepay.activity.data.di.activityDataModule
import com.laarasoft.moepay.activity.presentation.di.activityPresentationModule
import com.laarasoft.moepay.auth.presentation.di.authPresentationModule
import com.laarasoft.moepay.chat.data.di.chatDataModule
import com.laarasoft.moepay.chat.presentation.di.chatPresentationModule
import com.laarasoft.moepay.core.data.di.coreDataModule
import com.laarasoft.moepay.home.data.di.homeDataModule
import com.laarasoft.moepay.home.presentation.di.homePresentationModule
import com.laarasoft.moepay.onboarding.data.di.onboardingDataModule
import com.laarasoft.moepay.onboarding.presentation.di.onboardingPresentationModule
import com.laarasoft.moepay.payments.data.di.paymentsDataModule
import com.laarasoft.moepay.payments.presentation.di.paymentsPresentationModule
import com.laarasoft.moepay.wallet.data.di.walletDataModule
import com.laarasoft.moepay.wallet.presentation.di.walletPresentationModule
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
            homeDataModule,
            homePresentationModule,
            walletDataModule,
            walletPresentationModule,
            paymentsDataModule,
            paymentsPresentationModule,
            activityDataModule,
            activityPresentationModule,
            chatDataModule,
            chatPresentationModule,
        )
    }
}
