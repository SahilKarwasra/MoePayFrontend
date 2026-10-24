package com.laarasoft.moepay.activity.data.di

import com.laarasoft.moepay.activity.data.KtorActivityService
import com.laarasoft.moepay.activity.domain.ActivityService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val activityDataModule = module {
    singleOf(::KtorActivityService) bind ActivityService::class
}
