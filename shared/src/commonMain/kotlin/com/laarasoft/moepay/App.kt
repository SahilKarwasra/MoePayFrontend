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
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.laarasoft.moepay.auth.presentation.navigation.AuthGraphRoutes
import com.laarasoft.moepay.core.designsystem.theme.MoeTheme
import com.laarasoft.moepay.core.presentation.util.ObserveAsEvents
import com.laarasoft.moepay.navigation.NavigationRoot
import com.laarasoft.moepay.splash.SplashScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    darkTheme: Boolean = isSystemInDarkTheme(),
    viewModel: MainViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
        }
    }
}
