package com.laarasoft.moepay.core.data.di

import com.laarasoft.moepay.core.data.auth.DataStoreSessionStorage
import com.laarasoft.moepay.core.data.auth.KtorAuthService
import com.laarasoft.moepay.core.data.networking.HttpClientFactory
import com.laarasoft.moepay.core.domain.auth.AuthService
import com.laarasoft.moepay.core.domain.auth.SessionStorage
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Provides HttpClientEngine and DataStore<Preferences>. */
internal expect val platformCoreDataModule: Module

val coreDataModule = module {
    includes(platformCoreDataModule)

    single {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
    single<HttpClient> { HttpClientFactory(get(), get()).create(get()) }

    singleOf(::DataStoreSessionStorage) bind SessionStorage::class
    singleOf(::KtorAuthService) bind AuthService::class
}
