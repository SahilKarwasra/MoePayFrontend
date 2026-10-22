package com.laarasoft.moepay

import androidx.compose.ui.window.ComposeUIViewController
import com.laarasoft.moepay.di.initKoin

/** Called from Swift (`MainViewControllerKt.startKoinForIos()`) once, before the first view controller. */
fun startKoinForIos() {
    initKoin()
}

fun MainViewController() = ComposeUIViewController { App() }
