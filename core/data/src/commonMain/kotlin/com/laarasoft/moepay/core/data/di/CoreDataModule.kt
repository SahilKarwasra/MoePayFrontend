package com.laarasoft.moepay.core.data.di

import com.laarasoft.moepay.core.data.auth.KtorAuthService
import com.laarasoft.moepay.core.data.auth.SecureSessionStorage
import com.laarasoft.moepay.core.data.networking.HttpClientFactory
import com.laarasoft.moepay.core.data.preferences.DataStoreAppPreferences
import com.laarasoft.moepay.core.data.realtime.KtorRealtimeClient
import com.laarasoft.moepay.core.data.wallet.KtorWalletRepository
import com.laarasoft.moepay.core.domain.auth.AuthService
import com.laarasoft.moepay.core.domain.auth.SessionStorage
import com.laarasoft.moepay.core.domain.preferences.AppPreferences
import com.laarasoft.moepay.core.domain.realtime.RealtimeEvents
import com.laarasoft.moepay.core.domain.wallet.WalletRepository
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Provides HttpClientEngine, DataStore<Preferences> and SecureStore. */
internal expect val platformCoreDataModule: Module

val coreDataModule = module {
    includes(platformCoreDataModule)

    single { ApplicationScope() }
    single {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
    single<HttpClient> { HttpClientFactory(get(), get()).create(get()) }

    singleOf(::SecureSessionStorage) bind SessionStorage::class
    singleOf(::KtorAuthService) bind AuthService::class
    singleOf(::KtorRealtimeClient) bind RealtimeEvents::class
    singleOf(::KtorWalletRepository) bind WalletRepository::class
    singleOf(::DataStoreAppPreferences) bind AppPreferences::class
}
