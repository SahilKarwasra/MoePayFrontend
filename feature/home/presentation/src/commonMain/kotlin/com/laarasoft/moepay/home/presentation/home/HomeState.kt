package com.laarasoft.moepay.home.presentation.home

enum class HomeTab {
    HOME,
    ACTIVITY,
    PROFILE,
}

data class HomeState(
    val selectedTab: HomeTab = HomeTab.HOME,
    val showLogoutDialog: Boolean = false,
    val isLoggingOut: Boolean = false,
)
