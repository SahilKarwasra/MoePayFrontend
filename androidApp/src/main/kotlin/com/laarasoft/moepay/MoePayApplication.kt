package com.laarasoft.moepay

import android.app.Application
import com.laarasoft.moepay.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class MoePayApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@MoePayApplication)
        }
    }
}
