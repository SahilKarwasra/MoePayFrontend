package com.laarasoft.moepay.payments.data.di

import com.laarasoft.moepay.payments.data.KtorPaymentsService
import com.laarasoft.moepay.payments.domain.PaymentsService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val paymentsDataModule = module {
    singleOf(::KtorPaymentsService) bind PaymentsService::class
}
