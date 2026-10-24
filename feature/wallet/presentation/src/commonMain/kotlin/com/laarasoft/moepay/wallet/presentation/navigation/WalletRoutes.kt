package com.laarasoft.moepay.wallet.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.laarasoft.moepay.wallet.domain.BankTransferDirection
import com.laarasoft.moepay.wallet.presentation.bank.LinkBankRoot
import com.laarasoft.moepay.wallet.presentation.pin.PinFlowRoot
import com.laarasoft.moepay.wallet.presentation.pin.PinMode
import com.laarasoft.moepay.wallet.presentation.profile.ProfileRoot
import com.laarasoft.moepay.wallet.presentation.qr.MyQrRoot
import com.laarasoft.moepay.wallet.presentation.transfer.BankTransferRoot
import kotlinx.serialization.Serializable

// Enum arguments are carried by name: navigation-compose has no built-in NavType for
// enums on iOS, so an enum-typed route property crashes the graph at build time.
sealed interface WalletRoutes {
    @Serializable
    data class Pin(val modeName: String) : WalletRoutes {
        constructor(mode: PinMode) : this(mode.name)
        val mode: PinMode get() = PinMode.valueOf(modeName)
    }

    @Serializable
    data object LinkBank : WalletRoutes

    @Serializable
    data class BankTransfer(val directionName: String) : WalletRoutes {
        constructor(direction: BankTransferDirection) : this(direction.name)
        val direction: BankTransferDirection get() = BankTransferDirection.valueOf(directionName)
    }

    @Serializable
    data object MyQr : WalletRoutes

    @Serializable
    data object Profile : WalletRoutes
}

/** Screens pushed over the tabs from the eWallet (and from Home's shortcuts). */
fun NavGraphBuilder.walletDestinations(navController: NavController) {
    composable<WalletRoutes.Pin> { entry ->
        val route = entry.toRoute<WalletRoutes.Pin>()
        PinFlowRoot(
            mode = route.mode,
            onFinished = { navController.popBackStack() },
            onOpenReset = {
                navController.navigate(WalletRoutes.Pin(PinMode.RESET)) {
                    popUpTo<WalletRoutes.Pin> { inclusive = true }
                }
            },
        )
    }
    composable<WalletRoutes.LinkBank> {
        LinkBankRoot(
            onBack = { navController.navigateUp() },
            onLinked = { navController.popBackStack() },
        )
    }
    composable<WalletRoutes.BankTransfer> { entry ->
        val route = entry.toRoute<WalletRoutes.BankTransfer>()
        BankTransferRoot(
            direction = route.direction,
            onBack = { navController.navigateUp() },
            onFinished = { navController.popBackStack() },
            onLinkBank = { navController.navigate(WalletRoutes.LinkBank) },
            onResetPin = { navController.navigate(WalletRoutes.Pin(PinMode.RESET)) },
        )
    }
    composable<WalletRoutes.Profile> {
        ProfileRoot(
            onBack = { navController.navigateUp() },
            onSetPin = { navController.navigate(WalletRoutes.Pin(PinMode.SET)) },
            onChangePin = { navController.navigate(WalletRoutes.Pin(PinMode.CHANGE)) },
            onResetPin = { navController.navigate(WalletRoutes.Pin(PinMode.RESET)) },
            onLinkBank = { navController.navigate(WalletRoutes.LinkBank) },
        )
    }
    composable<WalletRoutes.MyQr> {
        MyQrRoot(onBack = { navController.navigateUp() })
    }
}
