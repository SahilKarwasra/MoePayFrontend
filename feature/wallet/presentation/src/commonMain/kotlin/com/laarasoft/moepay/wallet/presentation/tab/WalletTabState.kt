package com.laarasoft.moepay.wallet.presentation.tab

import com.laarasoft.moepay.core.domain.wallet.WalletOverview
import com.laarasoft.moepay.core.presentation.util.UiText

data class WalletTabState(
    val overview: WalletOverview? = null,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,
    val isBalanceHidden: Boolean = false,
    /** Signed-in user's name, for the avatar that opens Profile. */
    val displayName: String = "",
)

sealed interface WalletTabAction {
    data object OnAppear : WalletTabAction
    data object OnRefresh : WalletTabAction
    data object OnToggleBalance : WalletTabAction
    data object OnAddMoneyClick : WalletTabAction
    data object OnWithdrawClick : WalletTabAction
    data object OnMyQrClick : WalletTabAction
    data object OnHistoryClick : WalletTabAction
    data object OnSetPinClick : WalletTabAction
    data object OnProfileClick : WalletTabAction
    data object OnRequestsClick : WalletTabAction
    data class OnTransactionClick(val id: String) : WalletTabAction
}

sealed interface WalletDestination {
    data object AddMoney : WalletDestination
    data object Withdraw : WalletDestination
    data object MyQr : WalletDestination
    data object History : WalletDestination
    data object SetPin : WalletDestination
    data object LinkBank : WalletDestination
    data object Requests : WalletDestination
    data object Profile : WalletDestination
    data class Transaction(val id: String) : WalletDestination
}

sealed interface WalletTabEvent {
    data class Navigate(val destination: WalletDestination) : WalletTabEvent
}
