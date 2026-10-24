package com.laarasoft.moepay.payments.presentation.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.core.domain.money.MoneyRequest
import com.laarasoft.moepay.core.domain.realtime.RealtimeEvent
import com.laarasoft.moepay.core.domain.realtime.RealtimeEvents
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.domain.wallet.IdempotencyKeyHolder
import com.laarasoft.moepay.core.domain.wallet.WalletError
import com.laarasoft.moepay.core.presentation.util.UiText
import com.laarasoft.moepay.core.presentation.util.toUiText
import com.laarasoft.moepay.payments.domain.PaidRequest
import com.laarasoft.moepay.payments.domain.PaymentsService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RequestConfirmation {
    DECLINE,
    CANCEL,
}

data class RequestDetailState(
    val request: MoneyRequest? = null,
    val isLoading: Boolean = true,
    val error: UiText? = null,
    val actionError: UiText? = null,
    val confirmation: RequestConfirmation? = null,
    val isActing: Boolean = false,
    val isPinVisible: Boolean = false,
    val pinError: UiText? = null,
    val pinErrorKey: Int = 0,
    val isPaying: Boolean = false,
    val lockedMessage: UiText? = null,
    val paid: PaidRequest? = null,
)

sealed interface RequestDetailAction {
    data object OnPayClick : RequestDetailAction
    data class OnPinEntered(val pin: String) : RequestDetailAction
    data object OnDismissPin : RequestDetailAction
    data object OnDeclineClick : RequestDetailAction
    data object OnCancelClick : RequestDetailAction
    data object OnConfirm : RequestDetailAction
    data object OnDismissConfirmation : RequestDetailAction
    data object OnDismissLocked : RequestDetailAction
    data object OnForgotPin : RequestDetailAction
    data object OnRetry : RequestDetailAction
}

sealed interface RequestDetailEvent {
    data object OpenResetPin : RequestDetailEvent
}

class RequestDetailViewModel(
    private val requestId: String,
    private val paymentsService: PaymentsService,
    realtimeEvents: RealtimeEvents,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private val keys = IdempotencyKeyHolder()
    private val eventChannel = Channel<RequestDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(RequestDetailState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                load()
                hasLoadedInitialData = true
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), RequestDetailState())

    init {
        // The other person may pay, decline or cancel while this is open.
        realtimeEvents.events
            .filter { it is RealtimeEvent.MoneyRequestUpdated && it.request.id == requestId }
            .onEach { event -> _state.update { it.copy(request = (event as RealtimeEvent.MoneyRequestUpdated).request) } }
            .launchIn(viewModelScope)
    }

    fun onAction(action: RequestDetailAction) {
        when (action) {
            RequestDetailAction.OnPayClick -> _state.update { it.copy(isPinVisible = true, pinError = null, actionError = null) }
            is RequestDetailAction.OnPinEntered -> pay(action.pin)
            RequestDetailAction.OnDismissPin -> if (!_state.value.isPaying) _state.update { it.copy(isPinVisible = false) }
            RequestDetailAction.OnDeclineClick -> _state.update { it.copy(confirmation = RequestConfirmation.DECLINE) }
            RequestDetailAction.OnCancelClick -> _state.update { it.copy(confirmation = RequestConfirmation.CANCEL) }
            RequestDetailAction.OnDismissConfirmation -> _state.update { it.copy(confirmation = null) }
            RequestDetailAction.OnConfirm -> resolve()
            RequestDetailAction.OnDismissLocked -> _state.update { it.copy(lockedMessage = null) }
            RequestDetailAction.OnForgotPin -> {
                _state.update { it.copy(isPinVisible = false, lockedMessage = null) }
                viewModelScope.launch { eventChannel.send(RequestDetailEvent.OpenResetPin) }
            }
            RequestDetailAction.OnRetry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            paymentsService.moneyRequest(requestId)
                .onSuccess { request -> _state.update { it.copy(request = request) } }
                .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun pay(pin: String) {
        if (_state.value.isPaying) return
        val key = keys.keyFor(requestId)
        viewModelScope.launch {
            _state.update { it.copy(isPaying = true, pinError = null) }
            paymentsService.payRequest(requestId, pin, key)
                .onSuccess { paid ->
                    keys.invalidate()
                    _state.update { it.copy(isPaying = false, isPinVisible = false, request = paid.request, paid = paid) }
                }
                .onFailure { error ->
                    when (error) {
                        is WalletError.PinInvalid -> _state.update { it.copy(isPaying = false, pinError = error.toUiText(), pinErrorKey = it.pinErrorKey + 1) }
                        is WalletError.PinLocked -> _state.update { it.copy(isPaying = false, isPinVisible = false, lockedMessage = error.toUiText()) }
                        else -> {
                            _state.update { it.copy(isPaying = false, isPinVisible = false, actionError = error.toUiText()) }
                            load()
                        }
                    }
                }
        }
    }

    private fun resolve() {
        val confirmation = _state.value.confirmation ?: return
        _state.update { it.copy(confirmation = null, isActing = true, actionError = null) }
        viewModelScope.launch {
            val result = when (confirmation) {
                RequestConfirmation.DECLINE -> paymentsService.declineRequest(requestId)
                RequestConfirmation.CANCEL -> paymentsService.cancelRequest(requestId)
            }
            result
                .onSuccess { request -> _state.update { it.copy(request = request) } }
                .onFailure { error -> _state.update { it.copy(actionError = error.toUiText()) } }
            _state.update { it.copy(isActing = false) }
        }
    }
}
