package com.laarasoft.moepay.core.data.networking

import com.laarasoft.moepay.core.domain.util.DataError

/** Maps the backend `error.code` (preferred) or the HTTP status to a [DataError.Remote]. */
fun mapApiError(code: String?, status: Int): DataError.Remote {
    return when (code) {
        "VALIDATION_FAILED", "INVALID_REQUEST" -> DataError.Remote.VALIDATION_FAILED
        "INVALID_PHONE_NUMBER" -> DataError.Remote.INVALID_PHONE_NUMBER
        "OTP_INVALID" -> DataError.Remote.OTP_INVALID
        "OTP_EXPIRED" -> DataError.Remote.OTP_EXPIRED
        "OTP_ATTEMPTS_EXCEEDED" -> DataError.Remote.OTP_ATTEMPTS_EXCEEDED
        "OTP_RESEND_COOLDOWN" -> DataError.Remote.OTP_RESEND_COOLDOWN
        "TOKEN_INVALID", "SESSION_REVOKED" -> DataError.Remote.SESSION_EXPIRED
        "USER_INACTIVE" -> DataError.Remote.USER_INACTIVE
        "USERNAME_TAKEN" -> DataError.Remote.USERNAME_TAKEN
        "USERNAME_INVALID" -> DataError.Remote.USERNAME_INVALID
        "ONBOARDING_STEP_OUT_OF_ORDER" -> DataError.Remote.ONBOARDING_STEP_OUT_OF_ORDER
        "ONBOARDING_ALREADY_COMPLETED" -> DataError.Remote.ONBOARDING_ALREADY_COMPLETED
        "ONBOARDING_INCOMPLETE" -> DataError.Remote.ONBOARDING_INCOMPLETE
        "RATE_LIMITED" -> DataError.Remote.TOO_MANY_REQUESTS
        else -> when (status) {
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
}
