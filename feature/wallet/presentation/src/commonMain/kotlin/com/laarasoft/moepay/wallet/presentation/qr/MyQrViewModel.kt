package com.laarasoft.moepay.wallet.presentation.qr

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.domain.wallet.WalletRepository
import com.laarasoft.moepay.core.presentation.money.AmountInput
import com.laarasoft.moepay.core.presentation.money.AmountKey
import com.laarasoft.moepay.core.presentation.util.UiText
import com.laarasoft.moepay.core.presentation.util.toUiText
import com.laarasoft.moepay.wallet.domain.PaymentQr
import com.laarasoft.moepay.wallet.domain.WalletService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyQrState(
    val qr: PaymentQr? = null,
    val isLoading: Boolean = true,
    val error: UiText? = null,
    val noteState: TextFieldState = TextFieldState(),
    val isAmountSheetVisible: Boolean = false,
    val draftAmount: AmountInput = AmountInput(),
    val rejectKey: Int = 0,
    val maxPence: Long = Long.MAX_VALUE,
    val isCopied: Boolean = false,
)

sealed interface MyQrAction {
    data object OnEditAmountClick : MyQrAction
    data class OnKey(val key: AmountKey) : MyQrAction
    data object OnApplyAmount : MyQrAction
    data object OnClearAmount : MyQrAction
    data object OnDismissAmountSheet : MyQrAction
    data object OnApplyNote : MyQrAction
    data object OnCopied : MyQrAction
    data object OnRetry : MyQrAction
}

class MyQrViewModel(
    private val walletService: WalletService,
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private var appliedPence: Long? = null

    private val _state = MutableStateFlow(MyQrState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                _state.update { it.copy(maxPence = walletRepository.overview.value?.limits?.maxPerTransaction?.pence ?: Long.MAX_VALUE) }
                load()
                hasLoadedInitialData = true
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), MyQrState())

    fun onAction(action: MyQrAction) {
        when (action) {
            MyQrAction.OnEditAmountClick -> _state.update {
                it.copy(isAmountSheetVisible = true, draftAmount = AmountInput.fromPence(appliedPence ?: 0))
            }
            is MyQrAction.OnKey -> {
                val next = _state.value.draftAmount.press(action.key, _state.value.maxPence)
                _state.update { if (next == null) it.copy(rejectKey = it.rejectKey + 1) else it.copy(draftAmount = next) }
            }
            MyQrAction.OnApplyAmount -> {
                appliedPence = _state.value.draftAmount.pence.takeIf { it > 0 }
                _state.update { it.copy(isAmountSheetVisible = false) }
                load()
            }
            MyQrAction.OnClearAmount -> {
                appliedPence = null
                load()
            }
            MyQrAction.OnDismissAmountSheet -> _state.update { it.copy(isAmountSheetVisible = false) }
            MyQrAction.OnApplyNote -> load()
            MyQrAction.OnCopied -> viewModelScope.launch {
                _state.update { it.copy(isCopied = true) }
                delay(1_600L)
                _state.update { it.copy(isCopied = false) }
            }
            MyQrAction.OnRetry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            walletService.paymentQr(appliedPence, _state.value.noteState.text.toString())
                .onSuccess { qr -> _state.update { it.copy(qr = qr) } }
                .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
            _state.update { it.copy(isLoading = false) }
        }
    }
}
