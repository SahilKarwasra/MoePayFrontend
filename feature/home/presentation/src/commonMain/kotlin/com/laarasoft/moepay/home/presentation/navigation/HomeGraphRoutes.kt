package com.laarasoft.moepay.home.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.laarasoft.moepay.home.presentation.home.HomeRoot
import kotlinx.serialization.Serializable

sealed interface HomeGraphRoutes {
    @Serializable
    data object Graph : HomeGraphRoutes

    @Serializable
    data object Home : HomeGraphRoutes
}

/** Logging out clears the session; the app root observes that and returns to auth. */
fun NavGraphBuilder.homeGraph() {
    navigation<HomeGraphRoutes.Graph>(
        startDestination = HomeGraphRoutes.Home,
    ) {
        composable<HomeGraphRoutes.Home> {
            HomeRoot()
        }
    }
}
