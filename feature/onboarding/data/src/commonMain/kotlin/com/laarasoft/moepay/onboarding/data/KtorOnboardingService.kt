package com.laarasoft.moepay.onboarding.data

import com.laarasoft.moepay.core.data.networking.get
import com.laarasoft.moepay.core.data.networking.post
import com.laarasoft.moepay.core.data.networking.put
import com.laarasoft.moepay.core.domain.auth.SessionStorage
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Result
import com.laarasoft.moepay.core.domain.util.map
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.onboarding.data.dto.OnboardingStateDto
import com.laarasoft.moepay.onboarding.data.dto.SaveProfileRequest
import com.laarasoft.moepay.onboarding.data.dto.SaveUsernameRequest
import com.laarasoft.moepay.onboarding.data.dto.UsernameAvailabilityDto
import com.laarasoft.moepay.onboarding.data.mappers.toDomain
import com.laarasoft.moepay.onboarding.domain.OnboardingService
import com.laarasoft.moepay.onboarding.domain.OnboardingState
import com.laarasoft.moepay.onboarding.domain.ProfileInput
import com.laarasoft.moepay.onboarding.domain.UsernameAvailability
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.JsonObject

class KtorOnboardingService(
    private val httpClient: HttpClient,
    private val sessionStorage: SessionStorage,
) : OnboardingService {

    override suspend fun getState(): Result<OnboardingState, DataError.Remote> {
        return httpClient.get<OnboardingStateDto>(route = "/onboarding")
            .map { it.toDomain() }
            .onSuccess { syncSession(it) }
    }

    override suspend fun saveProfile(input: ProfileInput): Result<OnboardingState, DataError.Remote> {
        return httpClient.put<SaveProfileRequest, OnboardingStateDto>(
            route = "/onboarding/profile",
            body = SaveProfileRequest(
                firstName = input.firstName.trim(),
                lastName = input.lastName.trim(),
                email = input.email?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )
            .map { it.toDomain() }
            .onSuccess { syncSession(it) }
    }

    override suspend fun saveUsername(username: String): Result<OnboardingState, DataError.Remote> {
        return httpClient.put<SaveUsernameRequest, OnboardingStateDto>(
            route = "/onboarding/username",
            body = SaveUsernameRequest(username = username),
        )
            .map { it.toDomain() }
            .onSuccess { syncSession(it) }
    }

    override suspend fun complete(): Result<OnboardingState, DataError.Remote> {
        return httpClient.post<JsonObject, OnboardingStateDto>(
            route = "/onboarding/complete",
            body = JsonObject(emptyMap()),
        )
            .map { it.toDomain() }
            .onSuccess { syncSession(it) }
    }

    override suspend fun checkUsername(username: String): Result<UsernameAvailability, DataError.Remote> {
        return httpClient.get<UsernameAvailabilityDto>(
            route = "/usernames/availability",
            queryParams = mapOf("username" to username),
        ).map { it.toDomain() }
    }

    /** Keeps the stored session's step and profile in line with the server, so a relaunch resumes correctly. */
    private suspend fun syncSession(state: OnboardingState) {
        val current = sessionStorage.observeAuthInfo().firstOrNull() ?: return
        sessionStorage.set(
            current.copy(
                onboardingStep = state.nextStep,
                user = current.user.copy(
                    username = state.username,
                    firstName = state.firstName,
                    lastName = state.lastName,
                    email = state.email,
                ),
            )
        )
    }
}
