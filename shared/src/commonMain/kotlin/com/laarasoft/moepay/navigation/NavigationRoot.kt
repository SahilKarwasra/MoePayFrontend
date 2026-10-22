package com.laarasoft.moepay.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.laarasoft.moepay.AppStart
import com.laarasoft.moepay.auth.presentation.navigation.AuthGraphRoutes
import com.laarasoft.moepay.auth.presentation.navigation.authGraph
import com.laarasoft.moepay.core.domain.onboarding.OnboardingStep
import com.laarasoft.moepay.home.presentation.navigation.HomeGraphRoutes
import com.laarasoft.moepay.home.presentation.navigation.homeGraph
import com.laarasoft.moepay.onboarding.presentation.navigation.OnboardingGraphRoutes
import com.laarasoft.moepay.onboarding.presentation.navigation.onboardingGraph
import com.laarasoft.moepay.onboarding.presentation.navigation.toRoute

private const val TRANSITION_MS = 320

/**
 * Owns every cross-graph transition: auth -> onboarding/home, onboarding -> home.
 * Returning to auth on logout / session expiry is handled in [com.laarasoft.moepay.App].
 */
@Composable
fun NavigationRoot(
    navController: NavHostController,
    start: AppStart,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = when (start) {
            AppStart.Auth -> AuthGraphRoutes.Graph
            is AppStart.Onboarding -> OnboardingGraphRoutes.Graph
            AppStart.Home -> HomeGraphRoutes.Graph
        },
        // iOS-style push / pop on both platforms.
        enterTransition = {
            slideInHorizontally(tween(TRANSITION_MS, easing = FastOutSlowInEasing)) { it }
        },
        exitTransition = {
            slideOutHorizontally(tween(TRANSITION_MS, easing = FastOutSlowInEasing)) { -it / 4 } +
                fadeOut(tween(TRANSITION_MS))
        },
        popEnterTransition = {
            slideInHorizontally(tween(TRANSITION_MS, easing = FastOutSlowInEasing)) { -it / 4 } +
                fadeIn(tween(TRANSITION_MS))
        },
        popExitTransition = {
            slideOutHorizontally(tween(TRANSITION_MS, easing = FastOutSlowInEasing)) { it }
        },
        modifier = modifier,
    ) {
        authGraph(
            navController = navController,
            onAuthenticated = { nextStep ->
                val destination: Any = if (nextStep == OnboardingStep.COMPLETED) {
                    HomeGraphRoutes.Graph
                } else {
                    nextStep.toRoute()
                }
                navController.navigate(destination) {
                    popUpTo(AuthGraphRoutes.Graph) { inclusive = true }
                }
            },
        )
        onboardingGraph(
            navController = navController,
            startStep = (start as? AppStart.Onboarding)?.step ?: OnboardingStep.PROFILE,
            onCompleted = {
                navController.navigate(HomeGraphRoutes.Graph) {
                    popUpTo(OnboardingGraphRoutes.Graph) { inclusive = true }
                }
            },
        )
        homeGraph()
    }
}

