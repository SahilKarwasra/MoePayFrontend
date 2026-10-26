package com.laarasoft.moepay.core.presentation.util

import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.presentation.resources.error_amount_invalid
import com.laarasoft.moepay.core.presentation.resources.error_bank_account_exists
import com.laarasoft.moepay.core.presentation.resources.error_bank_account_limit
import com.laarasoft.moepay.core.presentation.resources.error_bank_account_not_found
import com.laarasoft.moepay.core.presentation.resources.error_bank_declined
import com.laarasoft.moepay.core.presentation.resources.error_cannot_pay_self
import com.laarasoft.moepay.core.presentation.resources.error_idempotency
import com.laarasoft.moepay.core.presentation.resources.error_insufficient_balance
import com.laarasoft.moepay.core.presentation.resources.error_limit_exceeded
import com.laarasoft.moepay.core.presentation.resources.error_client_message_id_reused
import com.laarasoft.moepay.core.presentation.resources.error_conversation_not_found
import com.laarasoft.moepay.core.presentation.resources.error_message_invalid
import com.laarasoft.moepay.core.presentation.resources.error_message_not_editable
import com.laarasoft.moepay.core.presentation.resources.error_message_not_found
import com.laarasoft.moepay.core.presentation.resources.error_message_version_conflict
import com.laarasoft.moepay.core.presentation.resources.error_money_request_not_found
import com.laarasoft.moepay.core.presentation.resources.error_payment_in_progress
import com.laarasoft.moepay.core.presentation.resources.error_pin_already_set
import com.laarasoft.moepay.core.presentation.resources.error_pin_format
import com.laarasoft.moepay.core.presentation.resources.error_pin_invalid
import com.laarasoft.moepay.core.presentation.resources.error_pin_locked
import com.laarasoft.moepay.core.presentation.resources.error_pin_not_set
import com.laarasoft.moepay.core.presentation.resources.error_pin_too_weak
import com.laarasoft.moepay.core.presentation.resources.error_recipient_cannot_receive
import com.laarasoft.moepay.core.presentation.resources.error_recipient_not_found
import com.laarasoft.moepay.core.presentation.resources.error_request_not_pending
import com.laarasoft.moepay.core.presentation.resources.error_transaction_not_found
import com.laarasoft.moepay.core.presentation.resources.error_wallet_balance_limit
import com.laarasoft.moepay.core.presentation.resources.error_wallet_frozen
import com.laarasoft.moepay.core.presentation.resources.error_wallet_not_ready
import com.laarasoft.moepay.core.presentation.resources.Res
import com.laarasoft.moepay.core.presentation.resources.error_bad_request
import com.laarasoft.moepay.core.presentation.resources.error_conflict
import com.laarasoft.moepay.core.presentation.resources.error_disk_full
import com.laarasoft.moepay.core.presentation.resources.error_forbidden
import com.laarasoft.moepay.core.presentation.resources.error_invalid_phone_number
import com.laarasoft.moepay.core.presentation.resources.error_no_internet
import com.laarasoft.moepay.core.presentation.resources.error_not_found
import com.laarasoft.moepay.core.presentation.resources.error_onboarding_already_completed
import com.laarasoft.moepay.core.presentation.resources.error_onboarding_incomplete
import com.laarasoft.moepay.core.presentation.resources.error_onboarding_out_of_order
import com.laarasoft.moepay.core.presentation.resources.error_otp_attempts_exceeded
import com.laarasoft.moepay.core.presentation.resources.error_otp_expired
import com.laarasoft.moepay.core.presentation.resources.error_otp_invalid
import com.laarasoft.moepay.core.presentation.resources.error_otp_resend_cooldown
import com.laarasoft.moepay.core.presentation.resources.error_request_timeout
import com.laarasoft.moepay.core.presentation.resources.error_serialization
import com.laarasoft.moepay.core.presentation.resources.error_server
import com.laarasoft.moepay.core.presentation.resources.error_service_unavailable
import com.laarasoft.moepay.core.presentation.resources.error_session_expired
import com.laarasoft.moepay.core.presentation.resources.error_too_many_requests
import com.laarasoft.moepay.core.presentation.resources.error_unauthorized
import com.laarasoft.moepay.core.presentation.resources.error_unknown
import com.laarasoft.moepay.core.presentation.resources.error_user_inactive
import com.laarasoft.moepay.core.presentation.resources.error_username_invalid
import com.laarasoft.moepay.core.presentation.resources.error_username_taken
import com.laarasoft.moepay.core.presentation.resources.error_validation_failed

fun DataError.toUiText(): UiText {
    val resource = when (this) {
        DataError.Local.DISK_FULL -> Res.string.error_disk_full
        DataError.Local.UNKNOWN -> Res.string.error_unknown

        DataError.Remote.BAD_REQUEST -> Res.string.error_bad_request
        DataError.Remote.UNAUTHORIZED -> Res.string.error_unauthorized
        DataError.Remote.FORBIDDEN -> Res.string.error_forbidden
        DataError.Remote.NOT_FOUND -> Res.string.error_not_found
        DataError.Remote.REQUEST_TIMEOUT -> Res.string.error_request_timeout
        DataError.Remote.CONFLICT -> Res.string.error_conflict
        DataError.Remote.TOO_MANY_REQUESTS -> Res.string.error_too_many_requests
        DataError.Remote.NO_INTERNET -> Res.string.error_no_internet
        DataError.Remote.SERVER_ERROR -> Res.string.error_server
        DataError.Remote.SERVICE_UNAVAILABLE -> Res.string.error_service_unavailable
        DataError.Remote.SERIALIZATION -> Res.string.error_serialization
        DataError.Remote.UNKNOWN -> Res.string.error_unknown

        DataError.Remote.VALIDATION_FAILED -> Res.string.error_validation_failed
        DataError.Remote.INVALID_PHONE_NUMBER -> Res.string.error_invalid_phone_number
        DataError.Remote.OTP_INVALID -> Res.string.error_otp_invalid
        DataError.Remote.OTP_EXPIRED -> Res.string.error_otp_expired
        DataError.Remote.OTP_ATTEMPTS_EXCEEDED -> Res.string.error_otp_attempts_exceeded
        DataError.Remote.OTP_RESEND_COOLDOWN -> Res.string.error_otp_resend_cooldown
        DataError.Remote.SESSION_EXPIRED -> Res.string.error_session_expired
        DataError.Remote.USER_INACTIVE -> Res.string.error_user_inactive
        DataError.Remote.USERNAME_TAKEN -> Res.string.error_username_taken
        DataError.Remote.USERNAME_INVALID -> Res.string.error_username_invalid
        DataError.Remote.ONBOARDING_STEP_OUT_OF_ORDER -> Res.string.error_onboarding_out_of_order
        DataError.Remote.ONBOARDING_ALREADY_COMPLETED -> Res.string.error_onboarding_already_completed
        DataError.Remote.ONBOARDING_INCOMPLETE -> Res.string.error_onboarding_incomplete

        DataError.Remote.WALLET_NOT_READY -> Res.string.error_wallet_not_ready
        DataError.Remote.WALLET_FROZEN -> Res.string.error_wallet_frozen
        DataError.Remote.INSUFFICIENT_BALANCE -> Res.string.error_insufficient_balance
        DataError.Remote.AMOUNT_INVALID -> Res.string.error_amount_invalid
        DataError.Remote.LIMIT_EXCEEDED -> Res.string.error_limit_exceeded
        DataError.Remote.WALLET_BALANCE_LIMIT -> Res.string.error_wallet_balance_limit
        DataError.Remote.RECIPIENT_CANNOT_RECEIVE -> Res.string.error_recipient_cannot_receive
        DataError.Remote.PIN_NOT_SET -> Res.string.error_pin_not_set
        DataError.Remote.PIN_ALREADY_SET -> Res.string.error_pin_already_set
        DataError.Remote.PIN_INVALID -> Res.string.error_pin_invalid
        DataError.Remote.PIN_LOCKED -> Res.string.error_pin_locked
        DataError.Remote.PIN_FORMAT -> Res.string.error_pin_format
        DataError.Remote.PIN_TOO_WEAK -> Res.string.error_pin_too_weak
        DataError.Remote.RECIPIENT_NOT_FOUND -> Res.string.error_recipient_not_found
        DataError.Remote.CANNOT_PAY_SELF -> Res.string.error_cannot_pay_self
        DataError.Remote.BANK_ACCOUNT_NOT_FOUND -> Res.string.error_bank_account_not_found
        DataError.Remote.BANK_ACCOUNT_EXISTS -> Res.string.error_bank_account_exists
        DataError.Remote.BANK_ACCOUNT_LIMIT -> Res.string.error_bank_account_limit
        DataError.Remote.BANK_DECLINED -> Res.string.error_bank_declined
        DataError.Remote.IDEMPOTENCY_KEY_REQUIRED -> Res.string.error_idempotency
        DataError.Remote.IDEMPOTENCY_KEY_REUSED -> Res.string.error_idempotency
        DataError.Remote.IDEMPOTENCY_KEY_IN_USE -> Res.string.error_payment_in_progress
        DataError.Remote.REQUEST_NOT_PENDING -> Res.string.error_request_not_pending
        DataError.Remote.TRANSACTION_NOT_FOUND -> Res.string.error_transaction_not_found
        DataError.Remote.MONEY_REQUEST_NOT_FOUND -> Res.string.error_money_request_not_found
        DataError.Remote.CONVERSATION_NOT_FOUND -> Res.string.error_conversation_not_found
        DataError.Remote.MESSAGE_NOT_FOUND -> Res.string.error_message_not_found
        DataError.Remote.MESSAGE_INVALID -> Res.string.error_message_invalid
        DataError.Remote.MESSAGE_NOT_EDITABLE -> Res.string.error_message_not_editable
        DataError.Remote.MESSAGE_VERSION_CONFLICT -> Res.string.error_message_version_conflict
        DataError.Remote.CLIENT_MESSAGE_ID_REUSED -> Res.string.error_client_message_id_reused
    }
    return UiText.Resource(resource)
}
