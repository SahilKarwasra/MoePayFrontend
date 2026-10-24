package com.laarasoft.moepay.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.laarasoft.moepay.AppStart
import com.laarasoft.moepay.activity.presentation.navigation.ActivityRoutes
import com.laarasoft.moepay.activity.presentation.navigation.activityDestinations
import com.laarasoft.moepay.auth.presentation.navigation.AuthGraphRoutes
import com.laarasoft.moepay.auth.presentation.navigation.authGraph
import com.laarasoft.moepay.core.domain.onboarding.OnboardingStep
import com.laarasoft.moepay.home.presentation.home.HomeDestination
import com.laarasoft.moepay.onboarding.presentation.navigation.OnboardingGraphRoutes
import com.laarasoft.moepay.onboarding.presentation.navigation.onboardingGraph
import com.laarasoft.moepay.onboarding.presentation.navigation.toRoute
import com.laarasoft.moepay.payments.presentation.navigation.PaymentsRoutes
import com.laarasoft.moepay.payments.presentation.navigation.paymentsDestinations
import com.laarasoft.moepay.wallet.domain.BankTransferDirection
import com.laarasoft.moepay.wallet.presentation.navigation.WalletRoutes
import com.laarasoft.moepay.wallet.presentation.navigation.walletDestinations
import com.laarasoft.moepay.wallet.presentation.pin.PinMode
import com.laarasoft.moepay.wallet.presentation.tab.WalletDestination
import kotlinx.serialization.Serializable

private const val TRANSITION_MS = 320

/** The signed-in app: tabs, with every feature's screens pushed on top. */
@Serializable
data object MainRoute

/**
 * Owns every cross-feature transition. Features expose destinations and report where the user
 * wants to go; this maps those intents to routes. Returning to auth on logout / session expiry
 * is handled in [com.laarasoft.moepay.App].
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
            AppStart.Home -> MainRoute
        },
        // iOS-style push / pop on both platforms.
        enterTransition = { slideInHorizontally(tween(TRANSITION_MS, easing = FastOutSlowInEasing)) { it } },
        exitTransition = {
            slideOutHorizontally(tween(TRANSITION_MS, easing = FastOutSlowInEasing)) { -it / 4 } + fadeOut(tween(TRANSITION_MS))
        },
        popEnterTransition = {
            slideInHorizontally(tween(TRANSITION_MS, easing = FastOutSlowInEasing)) { -it / 4 } + fadeIn(tween(TRANSITION_MS))
        },
        popExitTransition = { slideOutHorizontally(tween(TRANSITION_MS, easing = FastOutSlowInEasing)) { it } },
        modifier = modifier,
    ) {
        authGraph(
            navController = navController,
            onAuthenticated = { nextStep ->
                val destination: Any = if (nextStep == OnboardingStep.COMPLETED) MainRoute else nextStep.toRoute()
                navController.navigate(destination) {
                    popUpTo(AuthGraphRoutes.Graph) { inclusive = true }
                }
            },
        )
        onboardingGraph(
            navController = navController,
            startStep = (start as? AppStart.Onboarding)?.step ?: OnboardingStep.PROFILE,
            onCompleted = {
                navController.navigate(MainRoute) {
                    popUpTo(OnboardingGraphRoutes.Graph) { inclusive = true }
                }
            },
        )
        composable<MainRoute> {
            val pendingLink by DeepLinks.pending.collectAsState()
            LaunchedEffect(pendingLink) {
                DeepLinks.consume()?.let { navController.navigate(PaymentsRoutes.Transfer.fromQr(it)) }
            }
            MainTabs(
                onHomeNavigate = { navController.navigateTo(it) },
                onWalletNavigate = { navController.navigateTo(it) },
            )
        }
        walletDestinations(navController)
        paymentsDestinations(
            navController = navController,
            onOpenResetPin = { navController.navigate(WalletRoutes.Pin(PinMode.RESET)) },
        )
        activityDestinations(
            navController = navController,
            onOpenRequest = { navController.navigate(PaymentsRoutes.RequestDetail(it)) },
        )
    }
}

private fun NavController.navigateTo(destination: HomeDestination) {
    when (destination) {
        is HomeDestination.Send -> navigate(PaymentsRoutes.Transfer.send(destination.recipient))
        HomeDestination.Request -> navigate(PaymentsRoutes.Transfer.request())
        HomeDestination.Scan -> navigate(PaymentsRoutes.Scan)
        HomeDestination.AddMoney -> navigate(WalletRoutes.BankTransfer(BankTransferDirection.ADD_MONEY))
        HomeDestination.Withdraw -> navigate(WalletRoutes.BankTransfer(BankTransferDirection.WITHDRAW))
        HomeDestination.History -> navigate(ActivityRoutes.History)
        is HomeDestination.Transaction -> navigate(ActivityRoutes.TransactionDetail(destination.id))
        HomeDestination.Requests -> navigate(PaymentsRoutes.Requests)
        HomeDestination.SetPin -> navigate(WalletRoutes.Pin(PinMode.SET))
        HomeDestination.LinkBank -> navigate(WalletRoutes.LinkBank)
        HomeDestination.Profile -> navigate(WalletRoutes.Profile)
    }
}

private fun NavController.navigateTo(destination: WalletDestination) {
    when (destination) {
        WalletDestination.AddMoney -> navigate(WalletRoutes.BankTransfer(BankTransferDirection.ADD_MONEY))
        WalletDestination.Withdraw -> navigate(WalletRoutes.BankTransfer(BankTransferDirection.WITHDRAW))
        WalletDestination.MyQr -> navigate(WalletRoutes.MyQr)
        WalletDestination.History -> navigate(ActivityRoutes.History)
        WalletDestination.SetPin -> navigate(WalletRoutes.Pin(PinMode.SET))
        WalletDestination.LinkBank -> navigate(WalletRoutes.LinkBank)
        WalletDestination.Requests -> navigate(PaymentsRoutes.Requests)
        WalletDestination.Profile -> navigate(WalletRoutes.Profile)
        is WalletDestination.Transaction -> navigate(ActivityRoutes.TransactionDetail(destination.id))
    }
}
