package com.laarasoft.moepay.home.presentation.home

sealed interface HomeAction {
    data class OnTabSelected(val tab: HomeTab) : HomeAction
    data object OnLogoutClick : HomeAction
    data object OnConfirmLogout : HomeAction
    data object OnDismissLogoutDialog : HomeAction
}
