package com.laarasoft.moepay.core.data.networking

import co.touchlab.kermit.Logger as KermitLogger
import com.laarasoft.moepay.core.data.dto.RefreshRequest
import com.laarasoft.moepay.core.data.dto.TokenPairDto
import com.laarasoft.moepay.core.domain.auth.SessionStorage
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json

class HttpClientFactory(
    private val sessionStorage: SessionStorage,
    private val json: Json,
) {

    fun create(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            expectSuccess = false

            install(ContentNegotiation) {
                json(json)
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000L
                socketTimeoutMillis = 20_000L
                requestTimeoutMillis = 20_000L
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        KermitLogger.withTag("MoePayHttp").d { message }
                    }
                }
                level = LogLevel.ALL
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
            install(WebSockets) {
                pingIntervalMillis = 20_000L
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        sessionStorage.observeAuthInfo().firstOrNull()?.let {
                            BearerTokens(accessToken = it.accessToken, refreshToken = it.refreshToken)
                        }
                    }
                    sendWithoutRequest { request ->
                        !isUnauthenticatedPath(request.url.build().encodedPath)
                    }
                    refreshTokens {
                        if (isUnauthenticatedPath(response.call.request.url.encodedPath)) {
                            return@refreshTokens null
                        }
                        val authInfo = sessionStorage.observeAuthInfo().firstOrNull()
                            ?: return@refreshTokens null

                        val result = client.post<RefreshRequest, TokenPairDto>(
                            route = "/auth/refresh",
                            body = RefreshRequest(refreshToken = authInfo.refreshToken),
                            builder = { markAsRefreshTokenRequest() },
                        )
                        when (result) {
                            is Result.Success -> {
                                sessionStorage.set(
                                    authInfo.copy(
                                        accessToken = result.data.accessToken,
                                        refreshToken = result.data.refreshToken,
                                    )
                                )
                                BearerTokens(
                                    accessToken = result.data.accessToken,
                                    refreshToken = result.data.refreshToken,
                                )
                            }
                            is Result.Failure -> {
                                // Only a rejected refresh token ends the session; a network error must not log the user out.
                                if (result.error.isSessionEnding()) {
                                    sessionStorage.set(null)
                                }
                                null
                            }
                        }
                    }
                }
            }
        }
    }

    private fun DataError.Remote.isSessionEnding(): Boolean = when (this) {
        DataError.Remote.UNAUTHORIZED,
        DataError.Remote.SESSION_EXPIRED,
        DataError.Remote.USER_INACTIVE,
        DataError.Remote.VALIDATION_FAILED -> true
        else -> false
    }
}
