package com.laarasoft.moepay.onboarding.domain

import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Result

/** Every mutation returns the full state, whose [OnboardingState.nextStep] drives navigation. */
interface OnboardingService {
    suspend fun getState(): Result<OnboardingState, DataError.Remote>
    suspend fun saveProfile(input: ProfileInput): Result<OnboardingState, DataError.Remote>
    suspend fun saveUsername(username: String): Result<OnboardingState, DataError.Remote>
    suspend fun complete(): Result<OnboardingState, DataError.Remote>
    suspend fun checkUsername(username: String): Result<UsernameAvailability, DataError.Remote>
}
