package com.laarasoft.moepay.core.data.auth

import com.laarasoft.moepay.core.data.dto.LoginResponseDto
import com.laarasoft.moepay.core.data.dto.SendOtpRequest
import com.laarasoft.moepay.core.data.dto.SendOtpResponseDto
import com.laarasoft.moepay.core.data.dto.VerifyOtpRequest
import com.laarasoft.moepay.core.data.mappers.toDomain
import com.laarasoft.moepay.core.data.networking.post
import com.laarasoft.moepay.core.domain.auth.AuthInfo
import com.laarasoft.moepay.core.domain.auth.AuthService
import com.laarasoft.moepay.core.domain.auth.OtpChallenge
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.EmptyResult
import com.laarasoft.moepay.core.domain.util.Result
import com.laarasoft.moepay.core.domain.util.asEmptyResult
import com.laarasoft.moepay.core.domain.util.map
import com.laarasoft.moepay.core.domain.util.onSuccess
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import kotlinx.serialization.json.JsonObject

class KtorAuthService(
    private val httpClient: HttpClient,
) : AuthService {

    override suspend fun sendOtp(phoneNumber: String): Result<OtpChallenge, DataError.Remote> {
        return httpClient.post<SendOtpRequest, SendOtpResponseDto>(
            route = "/auth/send-otp",
            body = SendOtpRequest(phoneNumber = phoneNumber),
        ).map { it.toDomain() }
    }

    override suspend fun verifyOtp(phoneNumber: String, otp: String): Result<AuthInfo, DataError.Remote> {
        return httpClient.post<VerifyOtpRequest, LoginResponseDto>(
            route = "/auth/verify-otp",
            body = VerifyOtpRequest(phoneNumber = phoneNumber, otp = otp),
        )
            .onSuccess { clearCachedTokens() }
            .map { it.toDomain() }
    }

    override suspend fun logout(): EmptyResult<DataError.Remote> {
        return httpClient.post<JsonObject, JsonObject>(
            route = "/auth/logout",
            body = JsonObject(emptyMap()),
        )
            .also { clearCachedTokens() }
            .asEmptyResult()
    }

    /** Ktor caches bearer tokens after the first load; drop them whenever the session changes. */
    private fun clearCachedTokens() {
        httpClient.authProvider<BearerAuthProvider>()?.clearToken()
    }
}
