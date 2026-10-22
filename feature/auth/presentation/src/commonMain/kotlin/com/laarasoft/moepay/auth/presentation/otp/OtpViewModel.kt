package com.laarasoft.moepay.auth.presentation.otp

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.auth.domain.OtpValidator
import com.laarasoft.moepay.auth.domain.PhoneNumberValidator
import com.laarasoft.moepay.auth.presentation.resources.Res
import com.laarasoft.moepay.auth.presentation.resources.otp_resent
import com.laarasoft.moepay.core.domain.auth.AuthService
import com.laarasoft.moepay.core.domain.auth.SessionStorage
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.presentation.util.UiText
import com.laarasoft.moepay.core.presentation.util.toUiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OtpViewModel(
    private val phoneNumber: String,
    private val resendAfterSeconds: Int,
    private val authService: AuthService,
    private val sessionStorage: SessionStorage,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private var countdownJob: Job? = null

    private val eventChannel = Channel<OtpEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(
        OtpState(formattedPhoneNumber = PhoneNumberValidator.format(phoneNumber))
    )
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeOtp()
                startResendCountdown(resendAfterSeconds)
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = _state.value,
        )

    fun onAction(action: OtpAction) {
        when (action) {
            OtpAction.OnVerifyClick -> verify()
            OtpAction.OnResendClick -> resend()
            else -> Unit
        }
    }

    private fun observeOtp() {
        snapshotFlow { _state.value.otpState.text.toString() }
            .distinctUntilChanged()
            .onEach { otp ->
                val isComplete = OtpValidator.isComplete(otp)
                _state.update {
                    it.copy(
                        canVerify = isComplete,
                        error = if (otp.isNotEmpty()) null else it.error,
                    )
                }
                // Submit as soon as the last digit is typed.
                if (isComplete) verify()
            }
            .launchIn(viewModelScope)
    }

    private fun verify() {
        val otp = _state.value.otpState.text.toString()
        if (!OtpValidator.isComplete(otp) || _state.value.isVerifying) return

        viewModelScope.launch {
            _state.update { it.copy(isVerifying = true, error = null, info = null) }
            authService
                .verifyOtp(phoneNumber = phoneNumber, otp = otp)
                .onSuccess { authInfo ->
                    sessionStorage.set(authInfo)
                    _state.update { it.copy(isVerifying = false) }
                    eventChannel.send(OtpEvent.Verified(authInfo.onboardingStep))
                }
                .onFailure { error ->
                    _state.value.otpState.clearText()
                    _state.update { it.copy(isVerifying = false, error = error.toUiText()) }
                }
        }
    }

    private fun resend() {
        if (!_state.value.canResend) return

        viewModelScope.launch {
            _state.update { it.copy(isResending = true, error = null, info = null) }
            authService
                .sendOtp(phoneNumber)
                .onSuccess { challenge ->
                    _state.value.otpState.clearText()
                    _state.update {
                        it.copy(isResending = false, info = UiText.Resource(Res.string.otp_resent))
                    }
                    startResendCountdown(challenge.resendAfterSeconds)
                }
                .onFailure { error ->
                    _state.update { it.copy(isResending = false, error = error.toUiText()) }
                }
        }
    }

    private fun startResendCountdown(seconds: Int) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (remaining in seconds.coerceAtLeast(0) downTo 0) {
                _state.update { it.copy(resendSecondsLeft = remaining) }
                if (remaining > 0) delay(1_000L)
            }
        }
    }
}
