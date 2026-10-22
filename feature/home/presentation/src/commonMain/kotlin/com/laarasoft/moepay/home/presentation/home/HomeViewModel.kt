package com.laarasoft.moepay.home.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.core.domain.auth.AuthService
import com.laarasoft.moepay.core.domain.auth.SessionStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authService: AuthService,
    private val sessionStorage: SessionStorage,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.OnTabSelected -> _state.update { it.copy(selectedTab = action.tab) }
            HomeAction.OnLogoutClick -> _state.update { it.copy(showLogoutDialog = true) }
            HomeAction.OnDismissLogoutDialog -> _state.update { it.copy(showLogoutDialog = false) }
            HomeAction.OnConfirmLogout -> logout()
        }
    }

    private fun logout() {
        if (_state.value.isLoggingOut) return
        _state.update { it.copy(showLogoutDialog = false, isLoggingOut = true) }

        viewModelScope.launch {
            // Revoke on the server when reachable; the local session is cleared either way.
            authService.logout()
            sessionStorage.set(null)
        }
    }
}
