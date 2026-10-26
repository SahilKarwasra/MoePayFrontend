package com.laarasoft.moepay.core.domain.money


/**
 * Turns what was typed in the "who" field into a lookup: a UK mobile ("07911 123456",
 * "+44 7911 123456") or a username ("@alex", "alex"). Returns null until it's plausible.
 */
object RecipientQuery {
    private val usernamePattern = Regex("^[a-z][a-z0-9_.]{2,29}$")

    fun parse(raw: String): RecipientRef? {
        val text = raw.trim()
        if (text.isEmpty()) return null
        val digits = text.filter { it.isDigit() }
        val looksLikePhone = text.all { it.isDigit() || it == ' ' || it == '+' || it == '-' }
        if (looksLikePhone) {
            val national = when {
                digits.startsWith("44") && digits.length == 12 -> digits.drop(2)
                digits.startsWith("0") && digits.length == 11 -> digits.drop(1)
                else -> digits
            }
            return if (national.length == 10 && national.first() == '7') RecipientRef.ByPhone("+44$national") else null
        }
        val username = text.removePrefix("@").lowercase()
        return if (usernamePattern.matches(username)) RecipientRef.ByUsername(username) else null
    }
}
