package com.laarasoft.moepay.activity.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.activity.domain.ActivityService
import com.laarasoft.moepay.core.domain.money.Transaction
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.presentation.util.UiText
import com.laarasoft.moepay.core.presentation.util.toUiText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionDetailState(
    val transaction: Transaction? = null,
    val isLoading: Boolean = true,
    val error: UiText? = null,
    val isReferenceCopied: Boolean = false,
)

sealed interface TransactionDetailAction {
    data object OnRetry : TransactionDetailAction
    data object OnReferenceCopied : TransactionDetailAction
}

class TransactionDetailViewModel(
    private val transactionId: String,
    private val activityService: ActivityService,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private val _state = MutableStateFlow(TransactionDetailState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                load()
                hasLoadedInitialData = true
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), TransactionDetailState())

    fun onAction(action: TransactionDetailAction) {
        when (action) {
            TransactionDetailAction.OnRetry -> load()
            TransactionDetailAction.OnReferenceCopied -> viewModelScope.launch {
                _state.update { it.copy(isReferenceCopied = true) }
                delay(1_600L)
                _state.update { it.copy(isReferenceCopied = false) }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            activityService.transaction(transactionId)
                .onSuccess { t -> _state.update { it.copy(transaction = t) } }
                .onFailure { e -> _state.update { it.copy(error = e.toUiText()) } }
            _state.update { it.copy(isLoading = false) }
        }
    }
}
