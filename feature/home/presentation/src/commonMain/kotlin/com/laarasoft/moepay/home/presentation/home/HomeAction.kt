package com.laarasoft.moepay.home.presentation.home

import com.laarasoft.moepay.core.domain.money.Recipient
import com.laarasoft.moepay.core.domain.wallet.WalletAction

sealed interface HomeAction {
    data object OnRefresh : HomeAction
    data object OnToggleBalance : HomeAction
    data class OnQuickActionClick(val action: WalletAction) : HomeAction
    data class OnRecipientClick(val recipient: Recipient) : HomeAction
    data class OnTransactionClick(val transactionId: String) : HomeAction
    data object OnSeeAllActivityClick : HomeAction
    data object OnRequestsClick : HomeAction
    data object OnSetPinClick : HomeAction
    data object OnAvatarClick : HomeAction
    data object OnDismissBlocker : HomeAction
}
