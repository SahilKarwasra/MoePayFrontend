package com.laarasoft.moepay.chat.presentation.money

import androidx.compose.foundation.text.input.clearText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.chat.domain.ChatRepository
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.domain.wallet.IdempotencyKeyHolder
import com.laarasoft.moepay.core.domain.wallet.WalletError
import com.laarasoft.moepay.core.domain.wallet.WalletRepository
import com.laarasoft.moepay.core.presentation.money.AmountInput
import com.laarasoft.moepay.core.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Pay or request money inside a chat. Requests are saved on the device and sent by the
 * outbox (they need no PIN); payments go straight to the server with the PIN, under one
 * idempotency key per confirmed amount so a retry can never pay twice.
 */
private val outcomeUnknown = setOf(
    DataError.Remote.NO_INTERNET,
    DataError.Remote.REQUEST_TIMEOUT,
    DataError.Remote.SERVER_ERROR,
    DataError.Remote.SERVICE_UNAVAILABLE,
    DataError.Remote.SERIALIZATION,
    DataError.Remote.UNKNOWN,
)

class ChatMoneyViewModel(
    private val conversationId: String,
    private val chatRepository: ChatRepository,
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private val keys = IdempotencyKeyHolder()
    private val eventChannel = Channel<ChatMoneyEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ChatMoneyState())
    val state = combine(_state, walletRepository.overview) { state, overview ->
        state.copy(overview = overview ?: state.overview)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), ChatMoneyState())

    fun onAction(action: ChatMoneyAction) {
        when (action) {
            is ChatMoneyAction.OnOpen -> open(action.mode)
            ChatMoneyAction.OnDismiss -> if (!_state.value.isSubmitting) _state.update { it.copy(mode = null) }
            is ChatMoneyAction.OnKey -> {
                val current = state.value
                val next = current.amount.press(action.key, current.maxPence)
                _state.update { if (next == null) it.copy(rejectKey = it.rejectKey + 1) else it.copy(amount = next) }
            }
            ChatMoneyAction.OnContinue -> onContinue()
            is ChatMoneyAction.OnPinEntered -> pay(action.pin)
            ChatMoneyAction.OnDismissPin -> if (!_state.value.isSubmitting) _state.update { it.copy(isPinVisible = false, pinError = null) }
            ChatMoneyAction.OnForgotPin -> {
                _state.update { it.copy(isPinVisible = false, mode = null) }
                send(ChatMoneyEvent.OpenResetPin)
            }
            ChatMoneyAction.OnDismissLocked -> _state.update { it.copy(lockedMessage = null) }
            ChatMoneyAction.OnDismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun open(mode: ChatMoneyMode) {
        _state.value.noteState.clearText()
        _state.update { it.copy(mode = mode, amount = AmountInput(), error = null, pinError = null) }
        viewModelScope.launch {
            val name = chatRepository.observeConversation(conversationId).filterNotNull().first().peer.displayName
            _state.update { it.copy(peerName = name) }
            walletRepository.refresh()
        }
    }

    private fun onContinue() {
        val current = state.value
        if (!current.canContinue) {
            _state.update { it.copy(rejectKey = it.rejectKey + 1) }
            return
        }
        val note = current.noteState.text.toString()
        when (current.mode) {
            ChatMoneyMode.REQUEST -> viewModelScope.launch {
                chatRepository.requestMoney(conversationId, current.amount.pence, note)
                _state.update { it.copy(mode = null) }
                send(ChatMoneyEvent.Done)
            }
            ChatMoneyMode.SEND -> _state.update { it.copy(mode = null, isPinVisible = true, pinError = null) }
            null -> Unit
        }
    }

    private fun pay(pin: String) {
        val current = _state.value
        if (current.isSubmitting) return
        val amount = current.amount.pence
        val note = current.noteState.text.toString()
        val key = keys.keyFor(listOf(conversationId, amount, note))
        _state.update { it.copy(isSubmitting = true, pinError = null) }
        viewModelScope.launch {
            chatRepository.sendMoney(conversationId, key, amount, note, pin)
                .onSuccess {
                    keys.invalidate()
                    _state.update { it.copy(isSubmitting = false, isPinVisible = false) }
                    send(ChatMoneyEvent.Done)
                }
                .onFailure { error ->
                    when (error) {
                        is WalletError.PinInvalid -> _state.update {
                            it.copy(isSubmitting = false, pinError = error.toUiText(), pinErrorKey = it.pinErrorKey + 1)
                        }
                        is WalletError.PinLocked -> _state.update {
                            it.copy(isSubmitting = false, isPinVisible = false, lockedMessage = error.toUiText())
                        }
                        is WalletError.BankDeclined, is WalletError.Remote -> {
                            // A definitive answer means the next attempt is a new payment; when the
                            // outcome is unknown the same key is kept so trying again can't pay twice.
                            if (error !is WalletError.Remote || error.error !in outcomeUnknown) keys.invalidate()
                            _state.update { it.copy(isSubmitting = false, isPinVisible = false, error = error.toUiText()) }
                        }
                    }
                }
        }
    }

    private fun send(event: ChatMoneyEvent) {
        viewModelScope.launch { eventChannel.send(event) }
    }
}
