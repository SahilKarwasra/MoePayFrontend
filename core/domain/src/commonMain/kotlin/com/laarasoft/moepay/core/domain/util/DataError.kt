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

        // E-wallet codes
        WALLET_NOT_READY,
        WALLET_FROZEN,
        INSUFFICIENT_BALANCE,
        AMOUNT_INVALID,
        LIMIT_EXCEEDED,
        WALLET_BALANCE_LIMIT,
        RECIPIENT_CANNOT_RECEIVE,
        PIN_NOT_SET,
        PIN_ALREADY_SET,
        PIN_INVALID,
        PIN_LOCKED,
        PIN_FORMAT,
        PIN_TOO_WEAK,
        RECIPIENT_NOT_FOUND,
        CANNOT_PAY_SELF,
        BANK_ACCOUNT_NOT_FOUND,
        BANK_ACCOUNT_EXISTS,
        BANK_ACCOUNT_LIMIT,
        BANK_DECLINED,
        IDEMPOTENCY_KEY_REQUIRED,
        IDEMPOTENCY_KEY_REUSED,
        IDEMPOTENCY_KEY_IN_USE,
        REQUEST_NOT_PENDING,
        TRANSACTION_NOT_FOUND,
        MONEY_REQUEST_NOT_FOUND,
    }

    enum class Local : DataError {
        DISK_FULL,
        UNKNOWN,
    }
}
