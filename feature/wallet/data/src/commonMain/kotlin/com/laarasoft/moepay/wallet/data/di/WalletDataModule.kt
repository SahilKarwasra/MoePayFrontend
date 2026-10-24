package com.laarasoft.moepay.wallet.data.di

import com.laarasoft.moepay.wallet.data.KtorWalletService
import com.laarasoft.moepay.wallet.domain.WalletService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val walletDataModule = module {
    singleOf(::KtorWalletService) bind WalletService::class
}
