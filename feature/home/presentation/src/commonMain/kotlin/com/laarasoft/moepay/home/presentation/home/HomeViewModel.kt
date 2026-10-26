package com.laarasoft.moepay.home.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.core.domain.preferences.AppPreferences
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.wallet.ActionBlocker
import com.laarasoft.moepay.core.domain.wallet.WalletAction
import com.laarasoft.moepay.core.domain.wallet.WalletActionId
import com.laarasoft.moepay.core.presentation.util.toUiText
import com.laarasoft.moepay.home.domain.HomeRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val appPreferences: AppPreferences,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val eventChannel = Channel<HomeEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(HomeState())
    val state = combine(
        _state,
        homeRepository.dashboard,
        appPreferences.observeBalanceHidden(),
    ) { state, dashboard, isHidden ->
        state.copy(dashboard = dashboard, isBalanceHidden = isHidden)
    }
        .onStart {
            if (!hasLoadedInitialData) {
                refresh(showIndicator = false)
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HomeState(dashboard = homeRepository.dashboard.value),
        )

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnRefresh -> refresh(showIndicator = true)
            HomeAction.OnToggleBalance -> viewModelScope.launch {
                appPreferences.setBalanceHidden(!appPreferences.observeBalanceHidden().first())
            }
            is HomeAction.OnQuickActionClick -> onQuickAction(action.action)
            is HomeAction.OnRecipientClick -> _state.update { it.copy(personMenuFor = action.recipient) }
            is HomeAction.OnSendToRecipient -> {
                _state.update { it.copy(personMenuFor = null) }
                navigate(HomeDestination.Send(action.recipient))
            }
            is HomeAction.OnMessageRecipient -> {
                _state.update { it.copy(personMenuFor = null) }
                navigate(HomeDestination.Message(action.recipient.userId))
            }
            HomeAction.OnDismissPersonMenu -> _state.update { it.copy(personMenuFor = null) }
            is HomeAction.OnTransactionClick -> navigate(HomeDestination.Transaction(action.transactionId))
            HomeAction.OnSeeAllActivityClick -> navigate(HomeDestination.History)
            HomeAction.OnRequestsClick -> navigate(HomeDestination.Requests)
            HomeAction.OnSetPinClick -> navigate(HomeDestination.SetPin)
            HomeAction.OnAvatarClick -> navigate(HomeDestination.Profile)
            HomeAction.OnDismissBlocker -> _state.update { it.copy(blocker = null) }
        }
    }

    private fun refresh(showIndicator: Boolean) {
        viewModelScope.launch {
            if (showIndicator) _state.update { it.copy(isRefreshing = true) }
            homeRepository.refresh()
                .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
            _state.update { it.copy(isRefreshing = false, error = if (homeRepository.dashboard.value != null) null else it.error) }
        }
    }

    /** Disabled actions route to whatever fixes them instead of doing nothing. */
    private fun onQuickAction(action: WalletAction) {
        when (action.blocker) {
            null -> when (action.id) {
                WalletActionId.SEND -> navigate(HomeDestination.Send())
                WalletActionId.REQUEST -> navigate(HomeDestination.Request)
                WalletActionId.SCAN_QR -> navigate(HomeDestination.Scan)
                WalletActionId.ADD_MONEY -> navigate(HomeDestination.AddMoney)
                WalletActionId.WITHDRAW -> navigate(HomeDestination.Withdraw)
                WalletActionId.HISTORY -> navigate(HomeDestination.History)
                WalletActionId.UNKNOWN -> Unit
            }
            ActionBlocker.PIN_NOT_SET -> navigate(HomeDestination.SetPin)
            ActionBlocker.NO_BANK_ACCOUNT -> navigate(HomeDestination.LinkBank)
            ActionBlocker.INSUFFICIENT_BALANCE -> navigate(HomeDestination.AddMoney)
            else -> _state.update { it.copy(blocker = action.blocker) }
        }
    }

    private fun navigate(destination: HomeDestination) {
        viewModelScope.launch { eventChannel.send(HomeEvent.Navigate(destination)) }
    }
}
