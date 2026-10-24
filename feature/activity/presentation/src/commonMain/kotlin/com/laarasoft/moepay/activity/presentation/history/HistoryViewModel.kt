package com.laarasoft.moepay.activity.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.activity.domain.ActivityService
import com.laarasoft.moepay.activity.domain.HistoryFilter
import com.laarasoft.moepay.core.domain.money.Transaction
import com.laarasoft.moepay.core.domain.realtime.RealtimeEvent
import com.laarasoft.moepay.core.domain.realtime.RealtimeEvents
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.presentation.util.UiText
import com.laarasoft.moepay.core.presentation.util.toUiText
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

data class HistoryState(
    val filter: HistoryFilter = HistoryFilter.ALL,
    val items: List<Transaction> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,
)

sealed interface HistoryAction {
    data class OnFilterChange(val filter: HistoryFilter) : HistoryAction
    data object OnLoadMore : HistoryAction
    data object OnRefresh : HistoryAction
}

class HistoryViewModel(
    private val activityService: ActivityService,
    realtimeEvents: RealtimeEvents,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private var loadJob: Job? = null

    private val _state = MutableStateFlow(HistoryState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                load(reset = true)
                hasLoadedInitialData = true
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), HistoryState())

    init {
        realtimeEvents.events
            .filter { it is RealtimeEvent.WalletCredited || it is RealtimeEvent.WalletDebited }
            .onEach { load(reset = true, silent = true) }
            .launchIn(viewModelScope)
    }

    fun onAction(action: HistoryAction) {
        when (action) {
            is HistoryAction.OnFilterChange -> if (action.filter != _state.value.filter) {
                _state.update { it.copy(filter = action.filter, items = emptyList(), isLoading = true, error = null) }
                load(reset = true)
            }
            HistoryAction.OnLoadMore -> {
                val s = _state.value
                if (s.hasMore && !s.isLoadingMore && !s.isLoading) load(reset = false)
            }
            HistoryAction.OnRefresh -> {
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
            activityService.transactions(current.filter, if (reset) null else current.nextCursor)
                .onSuccess { page ->
                    _state.update {
                        it.copy(
                            items = if (reset) page.items else (it.items + page.items).distinctBy { t -> t.id },
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
