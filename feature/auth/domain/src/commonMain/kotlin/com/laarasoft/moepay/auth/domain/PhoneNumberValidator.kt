package com.laarasoft.moepay.auth.domain

/**
 * UK mobile numbers. Accepts "07911 123456" or "7911 123456" (spaces ignored) and
 * normalises to E.164 "+447911123456". The backend parses with region GB as well.
 */
object PhoneNumberValidator {
    const val COUNTRY_CODE = "+44"
    const val FLAG = "🇬🇧"

    /** Longest accepted input: a national number with its trunk "0". */
    const val MAX_INPUT_LENGTH = 11

    private const val NATIONAL_NUMBER_LENGTH = 10

    /** Drops spaces and the trunk prefix: "07911 123456" -> "7911123456". */
    fun nationalNumber(input: String): String =
        input.filter { it.isDigit() }.removePrefix("0")

    fun isValid(input: String): Boolean {
        val national = nationalNumber(input)
        return national.length == NATIONAL_NUMBER_LENGTH && national.first() == '7'
    }

    fun toE164(input: String): String = "$COUNTRY_CODE${nationalNumber(input)}"

    /** +447911123456 -> +44 7911 123456 */
    fun format(e164: String): String {
        if (!e164.startsWith(COUNTRY_CODE)) return e164
        val national = e164.removePrefix(COUNTRY_CODE)
        if (national.length != NATIONAL_NUMBER_LENGTH) return e164
        return "$COUNTRY_CODE ${national.take(4)} ${national.drop(4)}"
    }
}
