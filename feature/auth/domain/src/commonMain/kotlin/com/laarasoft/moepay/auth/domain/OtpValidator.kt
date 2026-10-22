package com.laarasoft.moepay.auth.domain

object OtpValidator {
    const val LENGTH = 6

    fun isComplete(otp: String): Boolean = otp.length == LENGTH && otp.all { it.isDigit() }
}
