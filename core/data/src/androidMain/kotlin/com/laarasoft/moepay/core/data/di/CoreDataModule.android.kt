package com.laarasoft.moepay.core.data.di

import com.laarasoft.moepay.core.data.connectivity.AndroidConnectivityObserver
import com.laarasoft.moepay.core.domain.connectivity.ConnectivityObserver
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.laarasoft.moepay.core.data.datastore.createDataStore
import com.laarasoft.moepay.core.data.session.PlatformSecureStore
import com.laarasoft.moepay.core.data.session.SecureStore
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal actual val platformCoreDataModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<DataStore<Preferences>> { createDataStore(androidContext()) }
    single<ConnectivityObserver> { AndroidConnectivityObserver(androidContext()) }
    single<SecureStore> { PlatformSecureStore(get()) }
}
