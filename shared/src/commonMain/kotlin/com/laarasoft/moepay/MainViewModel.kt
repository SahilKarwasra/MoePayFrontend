package com.laarasoft.moepay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.core.domain.auth.AuthInfo
import com.laarasoft.moepay.core.domain.auth.SessionStorage
import com.laarasoft.moepay.core.domain.onboarding.OnboardingStep
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val sessionStorage: SessionStorage,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val eventChannel = Channel<MainEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(MainState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                resolveStart()
                observeSessionEnd()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = MainState(),
        )

    private fun resolveStart() {
        viewModelScope.launch {
            // Keep the splash up long enough for its animation, in parallel with reading the session.
            val minimumSplash = async { delay(SPLASH_MIN_DURATION_MS) }
            val session = sessionStorage.observeAuthInfo().firstOrNull()
            minimumSplash.await()
            _state.update { it.copy(isReady = true, start = session.toAppStart()) }
        }
    }

    private fun observeSessionEnd() {
        sessionStorage
            .observeAuthInfo()
            .map { it != null }
            .distinctUntilChanged()
            .drop(1)
            .filter { isSignedIn -> !isSignedIn }
            .onEach { eventChannel.send(MainEvent.SessionEnded) }
            .launchIn(viewModelScope)
    }

    private fun AuthInfo?.toAppStart(): AppStart = when {
        this == null -> AppStart.Auth
        onboardingStep == OnboardingStep.COMPLETED -> AppStart.Home
        else -> AppStart.Onboarding(onboardingStep)
    }

    private companion object {
        const val SPLASH_MIN_DURATION_MS = 1_200L
    }
}
