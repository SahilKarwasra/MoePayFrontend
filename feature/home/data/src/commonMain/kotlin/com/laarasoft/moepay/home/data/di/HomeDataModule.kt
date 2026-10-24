package com.laarasoft.moepay.home.data.di

import com.laarasoft.moepay.home.data.KtorHomeRepository
import com.laarasoft.moepay.home.domain.HomeRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val homeDataModule = module {
    singleOf(::KtorHomeRepository) bind HomeRepository::class
}
