package com.laarasoft.moepay.payments.presentation.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.core.domain.money.MoneyRequest
import com.laarasoft.moepay.core.domain.money.MoneyRequestDirection
import com.laarasoft.moepay.core.domain.money.MoneyRequestStatus
import com.laarasoft.moepay.core.domain.realtime.RealtimeEvent
import com.laarasoft.moepay.core.domain.realtime.RealtimeEvents
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.presentation.util.UiText
import com.laarasoft.moepay.core.presentation.util.toUiText
import com.laarasoft.moepay.payments.domain.PaymentsService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MoneyRequestsState(
    val direction: MoneyRequestDirection = MoneyRequestDirection.INCOMING,
    val pendingOnly: Boolean = true,
    val items: List<MoneyRequest> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,
)

sealed interface MoneyRequestsAction {
    data object OnAppear : MoneyRequestsAction
    data class OnDirectionChange(val direction: MoneyRequestDirection) : MoneyRequestsAction
    data class OnPendingOnlyChange(val pendingOnly: Boolean) : MoneyRequestsAction
    data object OnLoadMore : MoneyRequestsAction
    data object OnRefresh : MoneyRequestsAction
}

class MoneyRequestsViewModel(
    private val paymentsService: PaymentsService,
    realtimeEvents: RealtimeEvents,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private var loadJob: Job? = null

    private val _state = MutableStateFlow(MoneyRequestsState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                load(reset = true)
                hasLoadedInitialData = true
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), MoneyRequestsState())

    init {
        realtimeEvents.events
            .filter { it is RealtimeEvent.MoneyRequestReceived || it is RealtimeEvent.MoneyRequestUpdated }
            .onEach { load(reset = true, silent = true) }
            .launchIn(viewModelScope)
    }

    fun onAction(action: MoneyRequestsAction) {
        when (action) {
            MoneyRequestsAction.OnAppear -> if (hasLoadedInitialData) load(reset = true, silent = true)
            is MoneyRequestsAction.OnDirectionChange -> if (action.direction != _state.value.direction) {
                _state.update { it.copy(direction = action.direction, items = emptyList(), isLoading = true) }
                load(reset = true)
            }
            is MoneyRequestsAction.OnPendingOnlyChange -> if (action.pendingOnly != _state.value.pendingOnly) {
                _state.update { it.copy(pendingOnly = action.pendingOnly, items = emptyList(), isLoading = true) }
                load(reset = true)
            }
            MoneyRequestsAction.OnLoadMore -> if (_state.value.hasMore && !_state.value.isLoadingMore && !_state.value.isLoading) load(reset = false)
            MoneyRequestsAction.OnRefresh -> {
                _state.update { it.copy(isRefreshing = true) }
                load(reset = true)
            }
        }
    }

    private fun load(reset: Boolean, silent: Boolean = false) {
        if (reset) loadJob?.cancel()
        val current = _state.value
        loadJob = viewModelScope.launch {
            if (!reset) _state.update { it.copy(isLoadingMore = true) }
            paymentsService.moneyRequests(
                direction = current.direction,
                status = if (current.pendingOnly) MoneyRequestStatus.PENDING else null,
                cursor = if (reset) null else current.nextCursor,
            )
                .onSuccess { page ->
                    _state.update {
                        it.copy(
                            items = if (reset) page.items else it.items + page.items,
                            nextCursor = page.nextCursor,
                            hasMore = page.hasMore,
                            error = null,
                        )
                    }
                }
                .onFailure { error -> if (!silent) _state.update { it.copy(error = error.toUiText()) } }
            _state.update { it.copy(isLoading = false, isLoadingMore = false, isRefreshing = false) }
        }
    }
}
