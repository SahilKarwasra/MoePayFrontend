package com.laarasoft.moepay.core.data.networking

import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Result
import com.laarasoft.moepay.core.domain.wallet.IdempotencyKey
import com.laarasoft.moepay.core.domain.wallet.WalletError
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import kotlinx.coroutines.delay

const val IDEMPOTENCY_HEADER = "Idempotency-Key"

fun HttpRequestBuilder.idempotencyKey(key: IdempotencyKey) {
    header(IDEMPOTENCY_HEADER, key.value)
}

/** Errors where the request may or may not have reached the server, so its outcome is unknown. */
private val outcomeUnknown = setOf(
    DataError.Remote.REQUEST_TIMEOUT,
    DataError.Remote.NO_INTERNET,
    DataError.Remote.SERVER_ERROR,
    DataError.Remote.SERVICE_UNAVAILABLE,
    DataError.Remote.SERIALIZATION,
    DataError.Remote.UNKNOWN,
)

/**
 * Runs a money-moving call that already carries an [IdempotencyKey]. Because the key is
 * reused, retrying here can never move money twice:
 * - outcome unknown: retry once, the server replays the original result if it ran;
 * - `IDEMPOTENCY_KEY_IN_USE`: the first attempt is still running, wait and retry.
 * PIN and business errors are returned immediately.
 */
suspend fun <T> retryIdempotent(call: suspend () -> Result<T, WalletError>): Result<T, WalletError> {
    var unknownRetries = 0
    var inFlightRetries = 0
    while (true) {
        val result = call()
        val error = (result as? Result.Failure)?.error as? WalletError.Remote ?: return result
        when {
            error.error == DataError.Remote.IDEMPOTENCY_KEY_IN_USE && inFlightRetries < 3 -> {
                inFlightRetries++
                delay(1_500L)
            }
            error.error in outcomeUnknown && unknownRetries < 1 -> {
                unknownRetries++
                delay(800L)
            }
            else -> return result
        }
    }
}
