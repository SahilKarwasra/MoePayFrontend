package com.laarasoft.moepay

import androidx.compose.ui.window.ComposeUIViewController
import com.laarasoft.moepay.core.designsystem.native.IosQrScanner
import com.laarasoft.moepay.core.designsystem.native.IosQrScannerRegistry
import com.laarasoft.moepay.di.initKoin
import com.laarasoft.moepay.navigation.DeepLinks

/** Called from Swift (`MainViewControllerKt.startKoinForIos()`) once, before the first view controller. */
fun startKoinForIos() {
    initKoin()
}

fun MainViewController() = ComposeUIViewController { App() }

/** Swift forwards `onOpenURL` here, e.g. a `moepay://pay?u=alex` payment link. */
fun handleDeepLink(url: String) {
    DeepLinks.onNewUri(url)
}

/**
 * Implemented in Swift with VisionKit's `DataScannerViewController` (a Swift-only API) and
 * registered at launch via [registerQrScanner].
 */
interface QrScannerBridge {
    fun isAvailable(): Boolean
    fun scan(onResult: (String?) -> Unit)
}

fun registerQrScanner(bridge: QrScannerBridge) {
    IosQrScannerRegistry.scanner = object : IosQrScanner {
        override val isAvailable: Boolean get() = bridge.isAvailable()
        override fun scan(onResult: (String?) -> Unit) = bridge.scan(onResult)
    }
}
