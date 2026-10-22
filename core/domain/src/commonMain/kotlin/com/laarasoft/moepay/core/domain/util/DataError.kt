package com.laarasoft.moepay.core.domain.util

sealed interface DataError : Error {

    /**
     * Transport and HTTP status failures, plus the stable error codes the MoePay
     * backend returns in `error.code` (see MoePay-Backend internal/apperr).
     */
    enum class Remote : DataError {
        // Transport / HTTP status
        BAD_REQUEST,
        UNAUTHORIZED,
        FORBIDDEN,
        NOT_FOUND,
        REQUEST_TIMEOUT,
        CONFLICT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        SERVER_ERROR,
        SERVICE_UNAVAILABLE,
        SERIALIZATION,
        UNKNOWN,

        // Backend error codes
        VALIDATION_FAILED,
        INVALID_PHONE_NUMBER,
        OTP_INVALID,
        OTP_EXPIRED,
        OTP_ATTEMPTS_EXCEEDED,
        OTP_RESEND_COOLDOWN,
        SESSION_EXPIRED,
        USER_INACTIVE,
        USERNAME_TAKEN,
        USERNAME_INVALID,
        ONBOARDING_STEP_OUT_OF_ORDER,
        ONBOARDING_ALREADY_COMPLETED,
        ONBOARDING_INCOMPLETE,
    }

    enum class Local : DataError {
        DISK_FULL,
        UNKNOWN,
    }
}
