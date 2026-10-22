package com.laarasoft.moepay.core.presentation.util

import com.laarasoft.moepay.core.domain.util.DataError
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
    }
    return UiText.Resource(resource)
}
