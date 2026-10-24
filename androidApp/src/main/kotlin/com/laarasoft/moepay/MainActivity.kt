package com.laarasoft.moepay

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.laarasoft.moepay.navigation.DeepLinks

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // The system splash uses the same logo and background as the Compose splash, so the
        // hand-off is seamless; the Compose splash then animates while the session loads.
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    /** `moepay://pay?u=…` links from other apps or a camera scan open the payment flow. */
    private fun handleDeepLink(intent: Intent?) {
        intent?.dataString?.let(DeepLinks::onNewUri)
    }
}
