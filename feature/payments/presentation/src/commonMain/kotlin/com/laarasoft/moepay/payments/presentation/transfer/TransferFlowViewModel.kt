package com.laarasoft.moepay.payments.presentation.transfer

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.core.domain.money.Recipient
import com.laarasoft.moepay.core.domain.money.RecipientRef
import com.laarasoft.moepay.core.domain.util.Result
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.domain.wallet.IdempotencyKeyHolder
import com.laarasoft.moepay.core.domain.wallet.WalletError
import com.laarasoft.moepay.core.domain.wallet.WalletRepository
import com.laarasoft.moepay.core.presentation.money.AmountInput
import com.laarasoft.moepay.core.presentation.util.toUiText
import com.laarasoft.moepay.payments.domain.PaymentsService
import com.laarasoft.moepay.payments.domain.RecipientQuery
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** How the flow was opened. */
data class TransferFlowArgs(
    val mode: TransferMode,
    val recipient: Recipient? = null,
    val qrPayload: String? = null,
)

class TransferFlowViewModel(
    private val args: TransferFlowArgs,
    private val paymentsService: PaymentsService,
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private val keys = IdempotencyKeyHolder()

    private val eventChannel = Channel<TransferFlowEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(
        TransferFlowState(
            mode = args.mode,
            recipient = args.recipient,
            recipientFixed = args.recipient != null || args.qrPayload != null,
            step = if (args.recipient != null) TransferStep.AMOUNT else TransferStep.RECIPIENT,
            isLoadingQr = args.qrPayload != null,
        )
    )
    val state = combine(_state, walletRepository.overview) { state, overview -> state.copy(overview = overview) }
        .onStart {
            if (!hasLoadedInitialData) {
                viewModelScope.launch { walletRepository.refresh() }
                args.qrPayload?.let(::resolveQr)
                if (args.recipient == null && args.qrPayload == null) {
                    loadRecents()
                    observeQuery()
                }
                hasLoadedInitialData = true
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), _state.value)

    fun onAction(action: TransferFlowAction) {
        when (action) {
            is TransferFlowAction.OnRecipientSelected -> _state.update {
                it.copy(recipient = action.recipient, step = TransferStep.AMOUNT, submitError = null)
            }
            is TransferFlowAction.OnKey -> onKey(action)
            TransferFlowAction.OnContinue -> if (canContinue()) _state.update { it.copy(isReviewVisible = true, submitError = null) }
            TransferFlowAction.OnDismissReview -> _state.update { it.copy(isReviewVisible = false) }
            TransferFlowAction.OnConfirm -> onConfirm()
            is TransferFlowAction.OnPinEntered -> send(action.pin)
            TransferFlowAction.OnDismissPin -> if (!_state.value.isSubmitting) _state.update { it.copy(isPinVisible = false, pinError = null) }
            TransferFlowAction.OnDismissLocked -> _state.update { it.copy(lockedMessage = null) }
            TransferFlowAction.OnBack -> onBack()
            TransferFlowAction.OnScanClick -> emit(TransferFlowEvent.OpenScanner)
            TransferFlowAction.OnDone -> emit(TransferFlowEvent.Close)
            TransferFlowAction.OnForgotPin -> {
                _state.update { it.copy(isPinVisible = false, lockedMessage = null) }
                emit(TransferFlowEvent.OpenResetPin)
            }
        }
    }

    private fun canContinue(): Boolean {
        val s = state.value
        return s.recipient != null && s.amount.pence >= s.minPence() && s.amount.pence <= s.maxPence()
    }

    private fun onKey(action: TransferFlowAction.OnKey) {
        if (_state.value.amountLocked) return
        val cap = state.value.overview?.limits?.maxPerTransaction?.pence ?: 500_000L
        val next = _state.value.amount.press(action.key, cap)
        _state.update { if (next == null) it.copy(rejectKey = it.rejectKey + 1) else it.copy(amount = next, submitError = null) }
    }

    private fun onConfirm() {
        when (_state.value.mode) {
            TransferMode.SEND -> _state.update { it.copy(isReviewVisible = false, isPinVisible = true, pinError = null) }
            TransferMode.REQUEST -> request()
        }
    }

    private fun send(pin: String) {
        val s = _state.value
        val recipient = s.recipient ?: return
        if (s.isSubmitting) return
        val note = s.noteState.text.toString().trim()
        val pence = s.amount.pence
        val key = keys.keyFor(Triple(recipient.userId, pence, note))

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, pinError = null) }
            paymentsService.transfer(RecipientRef.ByUserId(recipient.userId), pence, note, pin, key)
                .onSuccess { movement ->
                    keys.invalidate()
                    _state.update { it.copy(isSubmitting = false, isPinVisible = false, result = TransferResult.Sent(movement, recipient)) }
                }
                .onFailure { error ->
                    when (error) {
                        is WalletError.PinInvalid -> _state.update {
                            it.copy(isSubmitting = false, pinError = error.toUiText(), pinErrorKey = it.pinErrorKey + 1)
                        }
                        is WalletError.PinLocked -> _state.update {
                            it.copy(isSubmitting = false, isPinVisible = false, lockedMessage = error.toUiText())
                        }
                        is WalletError.BankDeclined, is WalletError.Remote -> _state.update {
                            it.copy(isSubmitting = false, isPinVisible = false, submitError = error.toUiText())
                        }
                    }
                }
        }
    }

    private fun request() {
        val s = _state.value
        val recipient = s.recipient ?: return
        if (s.isSubmitting) return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, submitError = null) }
            paymentsService.requestMoney(RecipientRef.ByUserId(recipient.userId), s.amount.pence, s.noteState.text.toString())
                .onSuccess { request -> _state.update { it.copy(isSubmitting = false, isReviewVisible = false, result = TransferResult.Requested(request)) } }
                .onFailure { error -> _state.update { it.copy(isSubmitting = false, isReviewVisible = false, submitError = error.toUiText()) } }
        }
    }

    private fun resolveQr(payload: String) {
        viewModelScope.launch {
            paymentsService.resolveQr(payload)
                .onSuccess { qr ->
                    qr.note?.let { _state.value.noteState.setTextAndPlaceCursorAtEnd(it) }
                    _state.update {
                        it.copy(
                            isLoadingQr = false,
                            recipient = qr.recipient,
                            step = TransferStep.AMOUNT,
                            amount = qr.amount?.let { m -> AmountInput.fromPence(m.pence) } ?: AmountInput(),
                            amountLocked = qr.amount != null,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoadingQr = false, recipientFixed = false, step = TransferStep.RECIPIENT, searchError = error.toUiText()) }
                    loadRecents()
                    observeQuery()
                }
        }
    }

    private fun loadRecents() {
        viewModelScope.launch {
            paymentsService.recentRecipients().onSuccess { list -> _state.update { it.copy(recents = list) } }
        }
    }

    private fun observeQuery() {
        viewModelScope.launch {
            snapshotFlow { _state.value.queryState.text.toString() }
                .distinctUntilChanged()
                .collectLatest { text ->
                    val ref = RecipientQuery.parse(text)
                    if (ref == null) {
                        _state.update { it.copy(isResolving = false, searchResult = null, searchError = null) }
                        return@collectLatest
                    }
                    _state.update { it.copy(isResolving = true, searchError = null) }
                    delay(350L) // debounce; cancelled by the next keystroke
                    when (val result = paymentsService.resolveRecipient(ref)) {
                        is Result.Success -> _state.update { it.copy(isResolving = false, searchResult = result.data) }
                        is Result.Failure -> _state.update { it.copy(isResolving = false, searchResult = null, searchError = result.error.toUiText()) }
                    }
                }
        }
    }

    private fun onBack() {
        val s = _state.value
        if (s.step == TransferStep.AMOUNT && !s.recipientFixed) {
            _state.update { it.copy(step = TransferStep.RECIPIENT, amount = if (it.amountLocked) AmountInput() else it.amount, amountLocked = false) }
        } else {
            emit(TransferFlowEvent.Close)
        }
    }

    private fun emit(event: TransferFlowEvent) {
        viewModelScope.launch { eventChannel.send(event) }
    }
}
