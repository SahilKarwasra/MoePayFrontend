package com.laarasoft.moepay.core.data.di

import com.laarasoft.moepay.core.data.connectivity.IosConnectivityObserver
import com.laarasoft.moepay.core.domain.connectivity.ConnectivityObserver
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.laarasoft.moepay.core.data.datastore.createDataStore
import com.laarasoft.moepay.core.data.session.PlatformSecureStore
import com.laarasoft.moepay.core.data.session.SecureStore
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.dsl.module

internal actual val platformCoreDataModule = module {
    single<HttpClientEngine> { Darwin.create() }
    single<DataStore<Preferences>> { createDataStore() }
    single<ConnectivityObserver> { IosConnectivityObserver() }
    single<SecureStore> { PlatformSecureStore() }
}
