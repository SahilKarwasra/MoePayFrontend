package com.laarasoft.moepay.activity.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.laarasoft.moepay.activity.presentation.detail.TransactionDetailRoot
import com.laarasoft.moepay.activity.presentation.history.HistoryRoot
import kotlinx.serialization.Serializable

sealed interface ActivityRoutes {
    @Serializable
    data object History : ActivityRoutes

    @Serializable
    data class TransactionDetail(val id: String) : ActivityRoutes
}

fun NavGraphBuilder.activityDestinations(
    navController: NavController,
    onOpenRequest: (String) -> Unit,
) {
    composable<ActivityRoutes.History> {
        HistoryRoot(
            onBack = { navController.navigateUp() },
            onTransactionClick = { navController.navigate(ActivityRoutes.TransactionDetail(it)) },
        )
    }
    composable<ActivityRoutes.TransactionDetail> { entry ->
        TransactionDetailRoot(
            transactionId = entry.toRoute<ActivityRoutes.TransactionDetail>().id,
            onBack = { navController.navigateUp() },
            onOpenRequest = onOpenRequest,
        )
    }
}
