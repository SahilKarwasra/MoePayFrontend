package com.laarasoft.moepay.auth.domain

/** Indian mobile numbers (the backend's default region is IN). */
object PhoneNumberValidator {
    const val COUNTRY_CODE = "+91"
    const val NATIONAL_NUMBER_LENGTH = 10

    fun isValid(nationalNumber: String): Boolean {
        return nationalNumber.length == NATIONAL_NUMBER_LENGTH &&
            nationalNumber.all { it.isDigit() } &&
            nationalNumber.first() in '6'..'9'
    }

    fun toE164(nationalNumber: String): String = "$COUNTRY_CODE$nationalNumber"

    /** +919876543210 -> +91 98765 43210 */
    fun format(e164: String): String {
        if (!e164.startsWith(COUNTRY_CODE) || e164.length != COUNTRY_CODE.length + NATIONAL_NUMBER_LENGTH) {
            return e164
        }
        val national = e164.removePrefix(COUNTRY_CODE)
        return "$COUNTRY_CODE ${national.take(5)} ${national.drop(5)}"
    }
}
