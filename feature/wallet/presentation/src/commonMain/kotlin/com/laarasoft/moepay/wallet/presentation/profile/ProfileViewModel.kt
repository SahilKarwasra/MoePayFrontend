package com.laarasoft.moepay.wallet.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.core.domain.auth.AuthService
import com.laarasoft.moepay.core.domain.auth.SessionStorage
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.domain.wallet.WalletRepository
import com.laarasoft.moepay.core.presentation.util.toUiText
import com.laarasoft.moepay.wallet.domain.WalletService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val walletRepository: WalletRepository,
    private val walletService: WalletService,
    private val sessionStorage: SessionStorage,
    private val authService: AuthService,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val eventChannel = Channel<ProfileEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ProfileState())
    val state = combine(
        _state,
        walletRepository.overview,
        sessionStorage.observeAuthInfo(),
    ) { state, overview, session ->
        state.copy(overview = overview, user = session?.user ?: state.user)
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
            initialValue = ProfileState(overview = walletRepository.overview.value),
        )

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnRefresh -> refresh(showIndicator = true)
            ProfileAction.OnBackClick -> send(ProfileEvent.Back)
            ProfileAction.OnPinClick -> {
                val pin = state.value.overview?.wallet?.pin
                send(
                    when {
                        pin == null || !pin.isSet -> ProfileEvent.SetPin
                        pin.isLocked -> ProfileEvent.ResetPin
                        else -> ProfileEvent.ChangePin
                    }
                )
            }
            ProfileAction.OnResetPinClick -> send(ProfileEvent.ResetPin)
            is ProfileAction.OnBankClick -> _state.update { it.copy(selectedBank = action.bank, bankActionError = null) }
            ProfileAction.OnDismissBankSheet -> _state.update { it.copy(selectedBank = null, bankActionError = null) }
            ProfileAction.OnMakePrimaryClick -> makePrimary()
            ProfileAction.OnRemoveBankClick -> _state.update { it.copy(bankPendingRemoval = it.selectedBank, selectedBank = null) }
            ProfileAction.OnDismissRemoveBank -> _state.update { it.copy(bankPendingRemoval = null) }
            ProfileAction.OnConfirmRemoveBank -> removeBank()
            ProfileAction.OnLinkBankClick -> send(ProfileEvent.LinkBank)
            ProfileAction.OnLogoutClick -> _state.update { it.copy(isLogoutDialogVisible = true) }
            ProfileAction.OnDismissLogoutDialog -> _state.update { it.copy(isLogoutDialogVisible = false) }
            ProfileAction.OnConfirmLogout -> logout()
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

    private fun makePrimary() {
        val bank = _state.value.selectedBank ?: return
        viewModelScope.launch {
            _state.update { it.copy(isBankActionInProgress = true, bankActionError = null) }
            walletService.makePrimary(bank.id)
                .onSuccess { _state.update { it.copy(selectedBank = null) } }
                .onFailure { error -> _state.update { it.copy(bankActionError = error.toUiText()) } }
            _state.update { it.copy(isBankActionInProgress = false) }
        }
    }

    private fun removeBank() {
        val bank = _state.value.bankPendingRemoval ?: return
        _state.update { it.copy(bankPendingRemoval = null) }
        viewModelScope.launch {
            walletService.removeBankAccount(bank.id)
                .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
        }
    }

    private fun logout() {
        if (_state.value.isLoggingOut) return
        _state.update { it.copy(isLogoutDialogVisible = false, isLoggingOut = true) }
        viewModelScope.launch {
            // Revoke on the server when reachable; the device session is cleared either way and
            // the app root returns to sign-in.
            authService.logout()
            sessionStorage.set(null)
        }
    }

    private fun send(event: ProfileEvent) {
        viewModelScope.launch { eventChannel.send(event) }
    }
}
