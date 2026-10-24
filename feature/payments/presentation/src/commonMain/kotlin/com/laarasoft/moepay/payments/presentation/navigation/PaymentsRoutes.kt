package com.laarasoft.moepay.payments.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.laarasoft.moepay.core.domain.money.Recipient
import com.laarasoft.moepay.payments.presentation.requests.MoneyRequestsRoot
import com.laarasoft.moepay.payments.presentation.requests.RequestDetailRoot
import com.laarasoft.moepay.payments.presentation.scan.ScanRoot
import com.laarasoft.moepay.payments.presentation.transfer.TransferFlowArgs
import com.laarasoft.moepay.payments.presentation.transfer.TransferFlowRoot
import com.laarasoft.moepay.payments.presentation.transfer.TransferMode
import kotlinx.serialization.Serializable

sealed interface PaymentsRoutes {
    /** Send or request; optionally prefilled with a recipient or a scanned QR payload. */
    // The mode travels by name: navigation-compose has no built-in NavType for enums on iOS.
    @Serializable
    data class Transfer(
        val modeName: String,
        val recipientUserId: String? = null,
        val recipientName: String? = null,
        val recipientUsername: String? = null,
        val recipientPhoneMasked: String? = null,
        val qrPayload: String? = null,
    ) : PaymentsRoutes {
        val mode: TransferMode get() = TransferMode.valueOf(modeName)

        companion object {
            fun send(recipient: Recipient? = null) = Transfer(
                modeName = TransferMode.SEND.name,
                recipientUserId = recipient?.userId,
                recipientName = recipient?.displayName,
                recipientUsername = recipient?.username,
                recipientPhoneMasked = recipient?.phoneMasked,
            )

            fun request() = Transfer(modeName = TransferMode.REQUEST.name)

            fun fromQr(payload: String) = Transfer(modeName = TransferMode.SEND.name, qrPayload = payload)
        }
    }

    @Serializable
    data object Scan : PaymentsRoutes

    @Serializable
    data object Requests : PaymentsRoutes

    @Serializable
    data class RequestDetail(val id: String) : PaymentsRoutes
}

private fun PaymentsRoutes.Transfer.toArgs() = TransferFlowArgs(
    mode = mode,
    recipient = recipientUserId?.let {
        Recipient(userId = it, username = recipientUsername, displayName = recipientName.orEmpty(), avatarUrl = null, phoneMasked = recipientPhoneMasked.orEmpty())
    },
    qrPayload = qrPayload,
)

fun NavGraphBuilder.paymentsDestinations(
    navController: NavController,
    onOpenResetPin: () -> Unit,
) {
    composable<PaymentsRoutes.Transfer> { entry ->
        val route = entry.toRoute<PaymentsRoutes.Transfer>()
        TransferFlowRoot(
            args = route.toArgs(),
            onClose = { navController.popBackStack() },
            onOpenScanner = {
                navController.navigate(PaymentsRoutes.Scan) {
                    popUpTo<PaymentsRoutes.Transfer> { inclusive = true }
                }
            },
            onOpenResetPin = onOpenResetPin,
        )
    }
    composable<PaymentsRoutes.Scan> {
        ScanRoot(
            onBack = { navController.navigateUp() },
            onPayload = { payload ->
                navController.navigate(PaymentsRoutes.Transfer.fromQr(payload)) {
                    popUpTo<PaymentsRoutes.Scan> { inclusive = true }
                }
            },
        )
    }
    composable<PaymentsRoutes.Requests> {
        MoneyRequestsRoot(
            onBack = { navController.navigateUp() },
            onRequestClick = { navController.navigate(PaymentsRoutes.RequestDetail(it)) },
        )
    }
    composable<PaymentsRoutes.RequestDetail> { entry ->
        val route = entry.toRoute<PaymentsRoutes.RequestDetail>()
        RequestDetailRoot(
            requestId = route.id,
            onBack = { navController.navigateUp() },
            onOpenResetPin = onOpenResetPin,
        )
    }
}
