package com.laarasoft.moepay.wallet.presentation.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.core.domain.money.MoneyMovement
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.domain.wallet.BankAccount
import com.laarasoft.moepay.core.domain.wallet.IdempotencyKeyHolder
import com.laarasoft.moepay.core.domain.wallet.WalletError
import com.laarasoft.moepay.core.domain.wallet.WalletOverview
import com.laarasoft.moepay.core.domain.wallet.WalletRepository
import com.laarasoft.moepay.core.presentation.money.AmountInput
import com.laarasoft.moepay.core.presentation.money.AmountKey
import com.laarasoft.moepay.core.presentation.util.UiText
import com.laarasoft.moepay.core.presentation.util.toUiText
import com.laarasoft.moepay.wallet.domain.BankTransferDirection
import com.laarasoft.moepay.wallet.domain.WalletService
import com.laarasoft.moepay.wallet.presentation.resources.Res
import com.laarasoft.moepay.wallet.presentation.resources.available_to_withdraw
import com.laarasoft.moepay.wallet.presentation.resources.below_minimum
import com.laarasoft.moepay.wallet.presentation.resources.can_add_up_to
import com.laarasoft.moepay.wallet.presentation.resources.over_limit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface BankTransferOutcome {
    data class Succeeded(val movement: MoneyMovement, val bank: BankAccount) : BankTransferOutcome
    data class Declined(val reason: UiText, val bank: BankAccount) : BankTransferOutcome
}

data class BankTransferState(
    val direction: BankTransferDirection,
    val overview: WalletOverview? = null,
    val selectedBankId: String? = null,
    val amount: AmountInput = AmountInput(),
    val rejectKey: Int = 0,
    val helperError: UiText? = null,
    val isBankPickerVisible: Boolean = false,
    val isReviewVisible: Boolean = false,
    val isPinVisible: Boolean = false,
    val pinError: UiText? = null,
    val pinErrorKey: Int = 0,
    val isSubmitting: Boolean = false,
    val lockedMessage: UiText? = null,
    val outcome: BankTransferOutcome? = null,
) {
    val selectedBank: BankAccount?
        get() = overview?.bankAccounts?.firstOrNull { it.id == selectedBankId } ?: overview?.primaryBankAccount

    /** The most this transfer may move: per-payment cap, and room left in the wallet or the day. */
    val maxPence: Long
        get() {
            val o = overview ?: return 0
            return when (direction) {
                BankTransferDirection.ADD_MONEY -> minOf(o.limits.maxPerTransaction.pence, o.limits.canAdd.pence)
                BankTransferDirection.WITHDRAW -> minOf(o.limits.maxPerTransaction.pence, o.wallet.balance.pence, o.limits.dailyOutgoing.remaining.pence)
            }
        }

    val helper: UiText?
        get() {
            helperError?.let { return it }
            val o = overview ?: return null
            if (amount.pence in 1 until o.limits.minPerTransaction.pence) {
                return UiText.Resource(Res.string.below_minimum, arrayOf(o.limits.minPerTransaction.display))
            }
            if (amount.pence > maxPence) return UiText.Resource(Res.string.over_limit)
            return when (direction) {
                BankTransferDirection.ADD_MONEY -> UiText.Resource(Res.string.can_add_up_to, arrayOf(o.limits.canAdd.display))
                BankTransferDirection.WITHDRAW -> UiText.Resource(
                    Res.string.available_to_withdraw,
                    arrayOf(o.wallet.balance.display, o.limits.dailyOutgoing.remaining.display),
                )
            }
        }

    val isHelperError: Boolean
        get() {
            if (helperError != null) return true
            val o = overview ?: return false
            return amount.pence > maxPence || amount.pence in 1 until o.limits.minPerTransaction.pence
        }

    val canContinue: Boolean
        get() {
            val o = overview ?: return false
            return selectedBank != null && amount.pence >= o.limits.minPerTransaction.pence && amount.pence <= maxPence
        }
}

sealed interface BankTransferAction {
    data class OnKey(val key: AmountKey) : BankTransferAction
    data object OnChangeBankClick : BankTransferAction
    data class OnBankSelected(val bankId: String) : BankTransferAction
    data object OnDismissBankPicker : BankTransferAction
    data object OnContinue : BankTransferAction
    data object OnDismissReview : BankTransferAction
    data object OnConfirm : BankTransferAction
    data class OnPinEntered(val pin: String) : BankTransferAction
    data object OnDismissPin : BankTransferAction
    data object OnDismissLocked : BankTransferAction
    data object OnTryAgain : BankTransferAction
}

class BankTransferViewModel(
    private val direction: BankTransferDirection,
    private val walletRepository: WalletRepository,
    private val walletService: WalletService,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private val keys = IdempotencyKeyHolder()

    private val _state = MutableStateFlow(BankTransferState(direction = direction))
    val state = combine(_state, walletRepository.overview) { state, overview -> state.copy(overview = overview) }
        .onStart {
            if (!hasLoadedInitialData) {
                viewModelScope.launch { walletRepository.refresh() }
                hasLoadedInitialData = true
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), BankTransferState(direction, walletRepository.overview.value))

    fun onAction(action: BankTransferAction) {
        when (action) {
            is BankTransferAction.OnKey -> onKey(action.key)
            BankTransferAction.OnChangeBankClick -> _state.update { it.copy(isBankPickerVisible = true) }
            is BankTransferAction.OnBankSelected -> _state.update { it.copy(selectedBankId = action.bankId, isBankPickerVisible = false, helperError = null) }
            BankTransferAction.OnDismissBankPicker -> _state.update { it.copy(isBankPickerVisible = false) }
            BankTransferAction.OnContinue -> if (state.value.canContinue) _state.update { it.copy(isReviewVisible = true) }
            BankTransferAction.OnDismissReview -> _state.update { it.copy(isReviewVisible = false) }
            BankTransferAction.OnConfirm -> _state.update { it.copy(isReviewVisible = false, isPinVisible = true, pinError = null) }
            is BankTransferAction.OnPinEntered -> submit(action.pin)
            BankTransferAction.OnDismissPin -> if (!_state.value.isSubmitting) _state.update { it.copy(isPinVisible = false, pinError = null) }
            BankTransferAction.OnDismissLocked -> _state.update { it.copy(lockedMessage = null) }
            BankTransferAction.OnTryAgain -> _state.update { it.copy(outcome = null) }
        }
    }

    private fun onKey(key: AmountKey) {
        val current = _state.value
        val cap = state.value.overview?.limits?.maxPerTransaction?.pence ?: Long.MAX_VALUE
        val next = current.amount.press(key, maxPence = cap)
        if (next == null) {
            _state.update { it.copy(rejectKey = it.rejectKey + 1) }
        } else {
            _state.update { it.copy(amount = next, helperError = null) }
        }
    }

    private fun submit(pin: String) {
        val snapshot = state.value
        val bank = snapshot.selectedBank ?: return
        if (snapshot.isSubmitting) return
        val amount = snapshot.amount.pence
        // Same key while retrying this exact transfer; a new one if anything about it changes.
        val key = keys.keyFor(Triple(direction, bank.id, amount))

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, pinError = null) }
            walletService.bankTransfer(direction, bank.id, amount, pin, key)
                .onSuccess { movement ->
                    keys.invalidate()
                    _state.update {
                        it.copy(isSubmitting = false, isPinVisible = false, outcome = BankTransferOutcome.Succeeded(movement, bank), amount = AmountInput())
                    }
                }
                .onFailure { error -> onError(error, bank) }
        }
    }

    private fun onError(error: WalletError, bank: BankAccount) {
        when (error) {
            is WalletError.PinInvalid -> _state.update {
                it.copy(isSubmitting = false, pinError = error.toUiText(), pinErrorKey = it.pinErrorKey + 1)
            }
            is WalletError.PinLocked -> _state.update {
                it.copy(isSubmitting = false, isPinVisible = false, lockedMessage = error.toUiText())
            }
            is WalletError.BankDeclined -> {
                // Recorded as failed by the server; the next attempt is a new transfer.
                keys.invalidate()
                _state.update {
                    it.copy(isSubmitting = false, isPinVisible = false, outcome = BankTransferOutcome.Declined(error.toUiText(), bank))
                }
            }
            is WalletError.Remote -> _state.update {
                it.copy(isSubmitting = false, isPinVisible = false, helperError = error.toUiText())
            }
        }
    }
}
