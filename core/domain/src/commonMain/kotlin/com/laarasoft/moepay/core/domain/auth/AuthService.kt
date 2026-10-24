package com.laarasoft.moepay.core.domain.auth

import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.EmptyResult
import com.laarasoft.moepay.core.domain.util.Result

interface AuthService {
    /** [phoneNumber] in E.164, e.g. +447911123456. */
    suspend fun sendOtp(phoneNumber: String): Result<OtpChallenge, DataError.Remote>

    /** On success the returned session is not persisted yet; the caller stores it. */
    suspend fun verifyOtp(phoneNumber: String, otp: String): Result<AuthInfo, DataError.Remote>

    /** Revokes the current session on the server. */
    suspend fun logout(): EmptyResult<DataError.Remote>
}
