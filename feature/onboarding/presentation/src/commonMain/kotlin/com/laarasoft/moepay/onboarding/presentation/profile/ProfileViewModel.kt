package com.laarasoft.moepay.onboarding.presentation.profile

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.presentation.util.toUiText
import com.laarasoft.moepay.onboarding.domain.OnboardingService
import com.laarasoft.moepay.onboarding.domain.ProfileInput
import com.laarasoft.moepay.onboarding.domain.ProfileValidator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val onboardingService: OnboardingService,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val eventChannel = Channel<ProfileEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ProfileState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                prefillSavedProfile()
                observeFields()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProfileState(),
        )

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnContinueClick -> save()
            else -> Unit
        }
    }

    /** Resuming onboarding (reinstall, other device): show what the server already has. */
    private fun prefillSavedProfile() {
        viewModelScope.launch {
            onboardingService.getState().onSuccess { saved ->
                val current = _state.value
                if (current.firstNameState.text.isEmpty()) saved.firstName?.let(current.firstNameState::setTextAndPlaceCursorAtEnd)
                if (current.lastNameState.text.isEmpty()) saved.lastName?.let(current.lastNameState::setTextAndPlaceCursorAtEnd)
                if (current.emailState.text.isEmpty()) saved.email?.let(current.emailState::setTextAndPlaceCursorAtEnd)
            }
        }
    }

    private fun observeFields() {
        combine(
            snapshotFlow { _state.value.firstNameState.text.toString() },
            snapshotFlow { _state.value.lastNameState.text.toString() },
            snapshotFlow { _state.value.emailState.text.toString() },
        ) { firstName, lastName, email ->
            val isEmailValid = ProfileValidator.isValidOptionalEmail(email)
            _state.update {
                it.copy(
                    isEmailValid = isEmailValid,
                    canContinue = ProfileValidator.isValidName(firstName) &&
                        ProfileValidator.isValidName(lastName) &&
                        isEmailValid,
                    error = null,
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun save() {
        val current = _state.value
        if (!current.canContinue || current.isSaving) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            onboardingService
                .saveProfile(
                    ProfileInput(
                        firstName = current.firstNameState.text.toString(),
                        lastName = current.lastNameState.text.toString(),
                        email = current.emailState.text.toString(),
                    )
                )
                .onSuccess { saved ->
                    _state.update { it.copy(isSaving = false) }
                    eventChannel.send(ProfileEvent.NextStep(saved.nextStep))
                }
                .onFailure { error ->
                    _state.update { it.copy(isSaving = false, error = error.toUiText()) }
                }
        }
    }
}
