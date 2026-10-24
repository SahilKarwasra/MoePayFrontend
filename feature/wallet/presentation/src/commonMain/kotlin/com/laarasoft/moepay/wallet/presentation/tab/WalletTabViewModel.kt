package com.laarasoft.moepay.wallet.presentation.tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.core.domain.auth.SessionStorage
import com.laarasoft.moepay.core.domain.preferences.AppPreferences
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.domain.wallet.ActionBlocker
import com.laarasoft.moepay.core.domain.wallet.WalletActionId
import com.laarasoft.moepay.core.domain.wallet.WalletRepository
import com.laarasoft.moepay.core.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WalletTabViewModel(
    private val walletRepository: WalletRepository,
    private val appPreferences: AppPreferences,
    private val sessionStorage: SessionStorage,
) : ViewModel() {

    private val eventChannel = Channel<WalletTabEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(WalletTabState())
    val state = combine(
        _state,
        walletRepository.overview,
        appPreferences.observeBalanceHidden(),
        sessionStorage.observeAuthInfo(),
    ) { state, overview, isHidden, session ->
        val user = session?.user
        state.copy(
            overview = overview,
            isBalanceHidden = isHidden,
            displayName = listOfNotNull(user?.firstName, user?.lastName).joinToString(" ").ifBlank { user?.username.orEmpty() },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = WalletTabState(overview = walletRepository.overview.value),
    )

    fun onAction(action: WalletTabAction) {
        when (action) {
            WalletTabAction.OnAppear -> refresh(showIndicator = false)
            WalletTabAction.OnRefresh -> refresh(showIndicator = true)
            WalletTabAction.OnToggleBalance -> viewModelScope.launch {
                appPreferences.setBalanceHidden(!appPreferences.observeBalanceHidden().first())
            }
            WalletTabAction.OnAddMoneyClick -> onWalletAction(WalletActionId.ADD_MONEY, WalletDestination.AddMoney)
            WalletTabAction.OnWithdrawClick -> onWalletAction(WalletActionId.WITHDRAW, WalletDestination.Withdraw)
            WalletTabAction.OnMyQrClick -> navigate(WalletDestination.MyQr)
            WalletTabAction.OnHistoryClick -> navigate(WalletDestination.History)
            WalletTabAction.OnSetPinClick -> navigate(WalletDestination.SetPin)
            WalletTabAction.OnProfileClick -> navigate(WalletDestination.Profile)
            WalletTabAction.OnRequestsClick -> navigate(WalletDestination.Requests)
            is WalletTabAction.OnTransactionClick -> navigate(WalletDestination.Transaction(action.id))
        }
    }

    private fun refresh(showIndicator: Boolean) {
        viewModelScope.launch {
            if (showIndicator) _state.update { it.copy(isRefreshing = true) }
            walletRepository.refresh()
                .onSuccess { _state.update { it.copy(error = null) } }
                .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    /** Disabled actions lead to the fix (PIN, bank) instead of doing nothing. */
    private fun onWalletAction(id: WalletActionId, destination: WalletDestination) {
        val action = state.value.overview?.actions?.firstOrNull { it.id == id }
        when (action?.blocker) {
            ActionBlocker.PIN_NOT_SET -> navigate(WalletDestination.SetPin)
            ActionBlocker.NO_BANK_ACCOUNT -> navigate(WalletDestination.LinkBank)
            else -> navigate(destination)
        }
    }

    private fun navigate(destination: WalletDestination) {
        viewModelScope.launch { eventChannel.send(WalletTabEvent.Navigate(destination)) }
    }
}
