package com.laarasoft.moepay.auth.presentation.phone

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.auth.domain.PhoneNumberValidator
import com.laarasoft.moepay.auth.presentation.resources.Res
import com.laarasoft.moepay.auth.presentation.resources.phone_error_invalid
import com.laarasoft.moepay.core.domain.auth.AuthService
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.presentation.util.UiText
import com.laarasoft.moepay.core.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
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

class PhoneNumberViewModel(
    private val authService: AuthService,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val eventChannel = Channel<PhoneNumberEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(PhoneNumberState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observePhoneNumber()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = PhoneNumberState(),
        )

    fun onAction(action: PhoneNumberAction) {
        when (action) {
            PhoneNumberAction.OnContinueClick -> sendOtp()
            else -> Unit
        }
    }

    private fun observePhoneNumber() {
        snapshotFlow { _state.value.phoneNumberState.text.toString() }
            .distinctUntilChanged()
            .onEach { number ->
                _state.update {
                    it.copy(
                        canContinue = PhoneNumberValidator.isValid(number),
                        error = null,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun sendOtp() {
        val number = _state.value.phoneNumberState.text.toString()
        if (!PhoneNumberValidator.isValid(number)) {
            _state.update { it.copy(error = UiText.Resource(Res.string.phone_error_invalid)) }
            return
        }
        if (_state.value.isSendingOtp) return

        viewModelScope.launch {
            _state.update { it.copy(isSendingOtp = true, error = null) }
            authService
                .sendOtp(PhoneNumberValidator.toE164(number))
                .onSuccess { challenge ->
                    _state.update { it.copy(isSendingOtp = false) }
                    eventChannel.send(
                        PhoneNumberEvent.OtpSent(
                            phoneNumber = challenge.phoneNumber,
                            resendAfterSeconds = challenge.resendAfterSeconds,
                        )
                    )
                }
                .onFailure { error ->
                    val message = when (error) {
                        DataError.Remote.INVALID_PHONE_NUMBER,
                        DataError.Remote.VALIDATION_FAILED -> UiText.Resource(Res.string.phone_error_invalid)
                        else -> error.toUiText()
                    }
                    _state.update { it.copy(isSendingOtp = false, error = message) }
                }
        }
    }
}
