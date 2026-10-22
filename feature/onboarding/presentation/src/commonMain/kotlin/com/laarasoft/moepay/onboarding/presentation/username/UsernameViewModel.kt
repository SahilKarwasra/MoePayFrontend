package com.laarasoft.moepay.onboarding.presentation.username

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Result
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.presentation.util.toUiText
import com.laarasoft.moepay.onboarding.domain.OnboardingService
import com.laarasoft.moepay.onboarding.domain.UsernameAvailability
import com.laarasoft.moepay.onboarding.domain.UsernameValidator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UsernameViewModel(
    private val onboardingService: OnboardingService,
) : ViewModel() {

    private var hasLoadedInitialData = false

    /** The username the server already has for this user (resumed onboarding). */
    private var savedUsername: String? = null

    private val eventChannel = Channel<UsernameEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(UsernameState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                prefillSavedUsername()
                observeAvailability()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = UsernameState(),
        )

    fun onAction(action: UsernameAction) {
        when (action) {
            UsernameAction.OnContinueClick -> save()
            else -> Unit
        }
    }

    private fun prefillSavedUsername() {
        viewModelScope.launch {
            onboardingService.getState().onSuccess { saved ->
                savedUsername = saved.username?.let(UsernameValidator::normalize)
                val field = _state.value.usernameState
                if (field.text.isEmpty()) saved.username?.let(field::setTextAndPlaceCursorAtEnd)
            }
        }
    }

    private fun observeAvailability() {
        viewModelScope.launch {
            snapshotFlow { _state.value.usernameState.text.toString() }
                .map(UsernameValidator::normalize)
                .distinctUntilChanged()
                .collectLatest { username ->
                    _state.update { it.copy(error = null) }
                    when {
                        username.isEmpty() -> setStatus(UsernameStatus.IDLE)
                        username.length < UsernameValidator.MIN_LENGTH -> setStatus(UsernameStatus.IDLE)
                        !UsernameValidator.isValid(username) -> setStatus(UsernameStatus.INVALID)
                        username == savedUsername -> setStatus(UsernameStatus.AVAILABLE)
                        else -> {
                            setStatus(UsernameStatus.CHECKING)
                            delay(400L) // debounce: cancelled by the next keystroke
                            when (val result = onboardingService.checkUsername(username)) {
                                is Result.Success -> setStatus(result.data.toStatus())
                                is Result.Failure -> {
                                    setStatus(UsernameStatus.IDLE)
                                    _state.update { it.copy(error = result.error.toUiText()) }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun save() {
        if (!_state.value.canContinue) return
        val username = UsernameValidator.normalize(_state.value.usernameState.text.toString())

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            onboardingService
                .saveUsername(username)
                .onSuccess { saved ->
                    savedUsername = username
                    _state.update { it.copy(isSaving = false) }
                    eventChannel.send(UsernameEvent.NextStep(saved.nextStep))
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            status = when (error) {
                                DataError.Remote.USERNAME_TAKEN -> UsernameStatus.TAKEN
                                DataError.Remote.USERNAME_INVALID -> UsernameStatus.INVALID
                                else -> it.status
                            },
                            error = error.toUiText(),
                        )
                    }
                }
        }
    }

    private fun setStatus(status: UsernameStatus) {
        _state.update { it.copy(status = status) }
    }

    private fun UsernameAvailability.toStatus(): UsernameStatus = when (this) {
        UsernameAvailability.AVAILABLE -> UsernameStatus.AVAILABLE
        UsernameAvailability.TAKEN -> UsernameStatus.TAKEN
        UsernameAvailability.RESERVED -> UsernameStatus.RESERVED
        UsernameAvailability.INVALID -> UsernameStatus.INVALID
    }
}
