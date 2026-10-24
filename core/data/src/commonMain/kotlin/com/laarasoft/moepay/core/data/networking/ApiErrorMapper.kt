package com.laarasoft.moepay.core.data.networking

import com.laarasoft.moepay.core.domain.util.DataError

/** Backend codes whose name differs from the client enum. */
private val aliases = mapOf(
    "INVALID_REQUEST" to DataError.Remote.VALIDATION_FAILED,
    "TOKEN_INVALID" to DataError.Remote.SESSION_EXPIRED,
    "SESSION_REVOKED" to DataError.Remote.SESSION_EXPIRED,
    "RATE_LIMITED" to DataError.Remote.TOO_MANY_REQUESTS,
    "INTERNAL_ERROR" to DataError.Remote.SERVER_ERROR,
)

private val byName = DataError.Remote.entries.associateBy { it.name }

/**
 * Maps the backend `error.code` (preferred) or the HTTP status to a [DataError.Remote].
 * Client enum names mirror MoePay-Backend `internal/apperr` codes one to one.
 */
fun mapApiError(code: String?, status: Int): DataError.Remote {
    if (code != null) {
        aliases[code]?.let { return it }
        byName[code]?.let { return it }
    }
    return when (status) {
        400 -> DataError.Remote.BAD_REQUEST
        401 -> DataError.Remote.UNAUTHORIZED
        403 -> DataError.Remote.FORBIDDEN
        404 -> DataError.Remote.NOT_FOUND
        408 -> DataError.Remote.REQUEST_TIMEOUT
        409 -> DataError.Remote.CONFLICT
        429 -> DataError.Remote.TOO_MANY_REQUESTS
        503 -> DataError.Remote.SERVICE_UNAVAILABLE
        in 500..599 -> DataError.Remote.SERVER_ERROR
        else -> DataError.Remote.UNKNOWN
    }
}
