package com.laarasoft.moepay.home.presentation.di

import com.laarasoft.moepay.home.presentation.home.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homePresentationModule = module {
    viewModelOf(::HomeViewModel)
}
