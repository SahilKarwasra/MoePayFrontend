package com.laarasoft.moepay

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.laarasoft.moepay.activity.presentation.navigation.ActivityRoutes
import com.laarasoft.moepay.auth.presentation.navigation.AuthGraphRoutes
import com.laarasoft.moepay.core.designsystem.theme.MoeTheme
import com.laarasoft.moepay.core.domain.realtime.RealtimeEvents
import com.laarasoft.moepay.core.presentation.util.ObserveAsEvents
import com.laarasoft.moepay.navigation.NavigationRoot
import com.laarasoft.moepay.payments.presentation.navigation.PaymentsRoutes
import com.laarasoft.moepay.realtime.BannerTarget
import com.laarasoft.moepay.realtime.RealtimeBanner
import com.laarasoft.moepay.splash.SplashScreen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    darkTheme: Boolean = isSystemInDarkTheme(),
    viewModel: MainViewModel = koinViewModel(),
    realtimeEvents: RealtimeEvents = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Live updates only while signed in and the app is visible; reconnects on return.
    LifecycleStartEffect(state.isSignedIn) {
        if (state.isSignedIn) realtimeEvents.start()
        onStopOrDispose { realtimeEvents.stop() }
    }

    MoeTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()

        ObserveAsEvents(viewModel.events) { event ->
            when (event) {
                MainEvent.SessionEnded -> {
                    navController.navigate(AuthGraphRoutes.Graph) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Crossfade(
                targetState = state.isReady,
                animationSpec = tween(durationMillis = 400),
                label = "splash",
            ) { isReady ->
                if (isReady) {
                    NavigationRoot(navController = navController, start = state.start)
                } else {
                    SplashScreen()
                }
            }
            if (state.isReady && state.isSignedIn) {
                RealtimeBanner(
                    onOpen = { target ->
                        when (target) {
                            is BannerTarget.Transaction -> navController.navigate(ActivityRoutes.TransactionDetail(target.id))
                            is BannerTarget.Request -> navController.navigate(PaymentsRoutes.RequestDetail(target.id))
                        }
                    },
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }
}
