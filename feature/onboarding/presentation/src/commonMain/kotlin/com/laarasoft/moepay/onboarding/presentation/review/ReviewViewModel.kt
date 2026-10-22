package com.laarasoft.moepay.onboarding.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.core.domain.onboarding.OnboardingStep
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.presentation.util.toUiText
import com.laarasoft.moepay.onboarding.domain.OnboardingService
import com.laarasoft.moepay.onboarding.domain.OnboardingState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val onboardingService: OnboardingService,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val eventChannel = Channel<ReviewEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ReviewState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadSummary()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ReviewState(),
        )

    fun onAction(action: ReviewAction) {
        when (action) {
            ReviewAction.OnFinishClick -> complete()
            else -> Unit
        }
    }

    private fun loadSummary() {
        viewModelScope.launch {
            onboardingService
                .getState()
                .onSuccess { saved -> _state.update { saved.toReviewState(it) } }
                .onFailure { error -> _state.update { it.copy(isLoading = false, error = error.toUiText()) } }
        }
    }

    private fun complete() {
        if (_state.value.isCompleting) return

        viewModelScope.launch {
            _state.update { it.copy(isCompleting = true, error = null) }
            onboardingService
                .complete()
                .onSuccess { saved ->
                    _state.update { it.copy(isCompleting = false) }
                    if (saved.nextStep == OnboardingStep.COMPLETED) {
                        eventChannel.send(ReviewEvent.Completed)
                    }
                }
                .onFailure { error ->
                    if (error == DataError.Remote.ONBOARDING_ALREADY_COMPLETED) {
                        _state.update { it.copy(isCompleting = false) }
                        eventChannel.send(ReviewEvent.Completed)
                    } else {
                        _state.update { it.copy(isCompleting = false, error = error.toUiText()) }
                    }
                }
        }
    }

    private fun OnboardingState.toReviewState(current: ReviewState): ReviewState {
        val name = listOfNotNull(firstName, lastName).joinToString(" ")
        val initials = listOfNotNull(firstName, lastName)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
        return current.copy(
            isLoading = false,
            fullName = name,
            initials = initials,
            username = username.orEmpty(),
            phoneNumber = phoneNumber,
            email = email,
        )
    }
}
