package com.laarasoft.moepay.wallet.presentation.bank

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.core.domain.auth.SessionStorage
import com.laarasoft.moepay.core.domain.util.Result
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.domain.wallet.WalletRepository
import com.laarasoft.moepay.core.presentation.util.UiText
import com.laarasoft.moepay.core.presentation.util.toUiText
import com.laarasoft.moepay.wallet.domain.BankDetailsValidator
import com.laarasoft.moepay.wallet.domain.LinkBankAccountInput
import com.laarasoft.moepay.wallet.domain.WalletService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LinkBankState(
    val holderState: TextFieldState = TextFieldState(),
    val sortCodeState: TextFieldState = TextFieldState(),
    val accountNumberState: TextFieldState = TextFieldState(),
    val makePrimary: Boolean = false,
    /** Primary choice only matters once another account exists. */
    val showMakePrimary: Boolean = false,
    val bankName: String? = null,
    val isLookingUpBank: Boolean = false,
    val isHolderInvalid: Boolean = false,
    val isSortCodeInvalid: Boolean = false,
    val isAccountNumberInvalid: Boolean = false,
    val canSubmit: Boolean = false,
    val isLinking: Boolean = false,
    val error: UiText? = null,
)

sealed interface LinkBankAction {
    data class OnMakePrimaryChange(val value: Boolean) : LinkBankAction
    data object OnSubmit : LinkBankAction
    data object OnBack : LinkBankAction
}

sealed interface LinkBankEvent {
    data object Linked : LinkBankEvent
}

class LinkBankViewModel(
    private val walletService: WalletService,
    private val walletRepository: WalletRepository,
    private val sessionStorage: SessionStorage,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private val eventChannel = Channel<LinkBankEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(LinkBankState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                prefill()
                observeFields()
                hasLoadedInitialData = true
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), LinkBankState())

    fun onAction(action: LinkBankAction) {
        when (action) {
            is LinkBankAction.OnMakePrimaryChange -> _state.update { it.copy(makePrimary = action.value) }
            LinkBankAction.OnSubmit -> submit()
            LinkBankAction.OnBack -> Unit
        }
    }

    /** The account must be in the user's name, so start from it. */
    private fun prefill() {
        viewModelScope.launch {
            val user = sessionStorage.observeAuthInfo().firstOrNull()?.user
            val name = listOfNotNull(user?.firstName, user?.lastName).joinToString(" ")
            if (name.isNotBlank() && _state.value.holderState.text.isEmpty()) {
                _state.value.holderState.setTextAndPlaceCursorAtEnd(name)
            }
            _state.update { it.copy(showMakePrimary = walletRepository.overview.value?.bankAccounts?.isNotEmpty() == true) }
        }
    }

    private fun observeFields() {
        val holder = snapshotFlow { _state.value.holderState.text.toString() }
        val sortCode = snapshotFlow { _state.value.sortCodeState.text.toString() }
        val account = snapshotFlow { _state.value.accountNumberState.text.toString() }

        combine(holder, sortCode, account) { h, s, a ->
            _state.update {
                it.copy(
                    isHolderInvalid = h.isNotBlank() && !BankDetailsValidator.isValidHolderName(h),
                    canSubmit = BankDetailsValidator.isValidHolderName(h) &&
                        BankDetailsValidator.isValidSortCode(s) &&
                        BankDetailsValidator.isValidAccountNumber(a),
                    isSortCodeInvalid = false,
                    isAccountNumberInvalid = false,
                    error = null,
                )
            }
        }.launchIn(viewModelScope)

        // Name the bank as soon as the sort code is complete.
        viewModelScope.launch {
            sortCode.map(BankDetailsValidator::digits).distinctUntilChanged().collectLatest { digits ->
                if (digits.length != BankDetailsValidator.SORT_CODE_DIGITS) {
                    _state.update { it.copy(bankName = null, isLookingUpBank = false) }
                    return@collectLatest
                }
                _state.update { it.copy(isLookingUpBank = true) }
                delay(200L)
                val name = (walletService.lookupSortCode(digits) as? Result.Success)?.data
                _state.update { it.copy(bankName = name, isLookingUpBank = false) }
            }
        }
    }

    private fun submit() {
        val current = _state.value
        if (current.isLinking) return
        val invalidSort = !BankDetailsValidator.isValidSortCode(current.sortCodeState.text.toString())
        val invalidAccount = !BankDetailsValidator.isValidAccountNumber(current.accountNumberState.text.toString())
        val invalidHolder = !BankDetailsValidator.isValidHolderName(current.holderState.text.toString())
        if (invalidSort || invalidAccount || invalidHolder) {
            _state.update { it.copy(isSortCodeInvalid = invalidSort, isAccountNumberInvalid = invalidAccount, isHolderInvalid = invalidHolder) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLinking = true, error = null) }
            walletService.linkBankAccount(
                LinkBankAccountInput(
                    holderName = current.holderState.text.toString(),
                    sortCode = current.sortCodeState.text.toString(),
                    accountNumber = current.accountNumberState.text.toString(),
                    makePrimary = current.makePrimary,
                )
            )
                .onSuccess { eventChannel.send(LinkBankEvent.Linked) }
                .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
            _state.update { it.copy(isLinking = false) }
        }
    }
}
