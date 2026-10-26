package com.laarasoft.moepay.home.presentation.home

import com.laarasoft.moepay.core.domain.money.Recipient

/** Where Home sends the user; the app root maps each to a route. */
sealed interface HomeDestination {
    data class Send(val recipient: Recipient? = null) : HomeDestination
    data object Request : HomeDestination
    data object Scan : HomeDestination
    data object AddMoney : HomeDestination
    data object Withdraw : HomeDestination
    data object History : HomeDestination
    data class Transaction(val id: String) : HomeDestination
    data object Requests : HomeDestination
    data object SetPin : HomeDestination
    data object LinkBank : HomeDestination
    data object Profile : HomeDestination
    data class Message(val userId: String) : HomeDestination
}

sealed interface HomeEvent {
    data class Navigate(val destination: HomeDestination) : HomeEvent
}
