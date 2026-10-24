package com.laarasoft.moepay.wallet.presentation.pin

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Result
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.domain.wallet.PinStatus
import com.laarasoft.moepay.core.domain.wallet.WalletError
import com.laarasoft.moepay.core.presentation.util.UiText
import com.laarasoft.moepay.core.presentation.util.formatPhoneForDisplay
import com.laarasoft.moepay.core.presentation.util.toUiText
import com.laarasoft.moepay.wallet.domain.PinProblem
import com.laarasoft.moepay.wallet.domain.PinRules
import com.laarasoft.moepay.wallet.domain.WalletService
import com.laarasoft.moepay.wallet.presentation.resources.Res
import com.laarasoft.moepay.wallet.presentation.resources.pin_mismatch
import com.laarasoft.moepay.wallet.presentation.resources.pin_weak
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val OTP_LENGTH = 6

/**
 * Set, change or reset the wallet PIN. Entered PINs and the OTP live only in private fields
 * here, never in screen state.
 */
class PinFlowViewModel(
    private val mode: PinMode,
    private val walletService: WalletService,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private var currentPin: String? = null
    private var newPin: String? = null
    private var otp: String? = null
    private var countdown: Job? = null

    private val eventChannel = Channel<PinFlowEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(
        PinFlowState(
            mode = mode,
            step = when (mode) {
                PinMode.SET -> PinStep.NEW
                PinMode.CHANGE -> PinStep.CURRENT
                PinMode.RESET -> PinStep.OTP
            },
        )
    )
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                if (mode == PinMode.RESET) {
                    sendOtp()
                    observeOtp()
                }
                hasLoadedInitialData = true
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), _state.value)

    fun onAction(action: PinFlowAction) {
        when (action) {
            is PinFlowAction.OnPinEntered -> onPinEntered(action.pin)
            PinFlowAction.OnResendOtp -> sendOtp()
            PinFlowAction.OnBack -> goBack()
            PinFlowAction.OnDone -> send(PinFlowEvent.Finished)
            PinFlowAction.OnResetPinFromLock -> {
                _state.update { it.copy(lockedMessage = null) }
                send(PinFlowEvent.OpenReset)
            }
            PinFlowAction.OnDismissLocked -> _state.update { it.copy(lockedMessage = null) }
        }
    }

    private fun onPinEntered(pin: String) {
        when (_state.value.step) {
            PinStep.CURRENT -> {
                currentPin = pin
                moveTo(PinStep.NEW)
            }
            PinStep.NEW -> {
                when (PinRules.problem(pin)) {
                    PinProblem.TOO_WEAK, PinProblem.FORMAT -> reject(PinStep.NEW, UiText.Resource(Res.string.pin_weak))
                    null -> {
                        newPin = pin
                        moveTo(PinStep.CONFIRM)
                    }
                }
            }
            PinStep.CONFIRM -> if (pin != newPin) {
                newPin = null
                reject(PinStep.NEW, UiText.Resource(Res.string.pin_mismatch))
            } else {
                submit(pin)
            }
            PinStep.OTP, PinStep.DONE -> Unit
        }
    }

    private fun submit(confirmPin: String) {
        val chosen = newPin ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result: Result<PinStatus, WalletError> = when (mode) {
                PinMode.SET -> walletService.setPin(chosen, confirmPin)
                PinMode.CHANGE -> walletService.changePin(currentPin.orEmpty(), chosen)
                PinMode.RESET -> walletService.resetPin(otp.orEmpty(), chosen)
            }
            _state.update { it.copy(isLoading = false) }
            result
                .onSuccess {
                    clearSecrets()
                    moveTo(PinStep.DONE)
                }
                .onFailure(::handleError)
        }
    }

    private fun handleError(error: WalletError) {
        when (error) {
            // Only the change flow sends a current PIN.
            is WalletError.PinInvalid -> reject(PinStep.CURRENT, error.toUiText())
            is WalletError.PinLocked -> {
                clearSecrets()
                _state.update { it.copy(step = PinStep.CURRENT, lockedMessage = error.toUiText()) }
            }
            is WalletError.Remote -> when (error.error) {
                DataError.Remote.OTP_INVALID, DataError.Remote.OTP_EXPIRED, DataError.Remote.OTP_ATTEMPTS_EXCEEDED -> {
                    otp = null
                    _state.value.otpState.clearText()
                    reject(PinStep.OTP, error.toUiText())
                }
                DataError.Remote.PIN_TOO_WEAK, DataError.Remote.PIN_FORMAT, DataError.Remote.VALIDATION_FAILED ->
                    reject(PinStep.NEW, if (error.error == DataError.Remote.VALIDATION_FAILED) UiText.Resource(Res.string.pin_weak) else error.toUiText())
                else -> reject(PinStep.CONFIRM, error.toUiText())
            }
            is WalletError.BankDeclined -> reject(PinStep.CONFIRM, error.toUiText())
        }
    }

    private fun sendOtp() {
        if (_state.value.isSendingOtp || _state.value.resendSecondsLeft > 0) return
        viewModelScope.launch {
            _state.update { it.copy(isSendingOtp = true, error = null) }
            walletService.sendPinResetOtp()
                .onSuccess { challenge ->
                    _state.update { it.copy(otpPhone = formatPhoneForDisplay(challenge.phoneNumber)) }
                    startCountdown(challenge.resendAfterSeconds)
                }
                .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
            _state.update { it.copy(isSendingOtp = false) }
        }
    }

    private fun observeOtp() {
        snapshotFlow { _state.value.otpState.text.toString() }
            .distinctUntilChanged()
            .onEach { if (it.isNotEmpty()) _state.update { s -> s.copy(error = null) } }
            .filter { it.length == OTP_LENGTH && _state.value.step == PinStep.OTP }
            .onEach { code ->
                otp = code
                moveTo(PinStep.NEW)
            }
            .launchIn(viewModelScope)
    }

    private fun startCountdown(seconds: Int) {
        countdown?.cancel()
        countdown = viewModelScope.launch {
            for (left in seconds.coerceAtLeast(0) downTo 0) {
                _state.update { it.copy(resendSecondsLeft = left) }
                if (left > 0) delay(1_000L)
            }
        }
    }

    private fun goBack() {
        val previous = when (_state.value.step) {
            PinStep.CONFIRM -> PinStep.NEW
            PinStep.NEW -> when (mode) {
                PinMode.SET -> null
                PinMode.CHANGE -> PinStep.CURRENT
                PinMode.RESET -> PinStep.OTP
            }
            else -> null
        }
        if (previous == null) {
            send(PinFlowEvent.Finished)
        } else {
            if (previous == PinStep.OTP) _state.value.otpState.clearText()
            moveTo(previous)
        }
    }

    private fun moveTo(step: PinStep) = _state.update { it.copy(step = step, error = null) }

    private fun reject(step: PinStep, message: UiText) =
        _state.update { it.copy(step = step, error = message, errorKey = it.errorKey + 1) }

    private fun clearSecrets() {
        currentPin = null
        newPin = null
        otp = null
    }

    private fun send(event: PinFlowEvent) {
        viewModelScope.launch { eventChannel.send(event) }
    }
}
