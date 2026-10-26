package com.laarasoft.moepay.home.presentation.home

import com.laarasoft.moepay.core.domain.money.Recipient
import com.laarasoft.moepay.core.domain.wallet.ActionBlocker
import com.laarasoft.moepay.core.presentation.util.UiText
import com.laarasoft.moepay.home.domain.HomeDashboard

data class HomeState(
    val dashboard: HomeDashboard? = null,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,
    val isBalanceHidden: Boolean = false,
    /** The person whose "Send money / Message" menu is open. */
    val personMenuFor: Recipient? = null,
    /** A disabled action was tapped and can't be fixed from here. */
    val blocker: ActionBlocker? = null,
) {
    val isInitialLoading: Boolean get() = dashboard == null && error == null
}
