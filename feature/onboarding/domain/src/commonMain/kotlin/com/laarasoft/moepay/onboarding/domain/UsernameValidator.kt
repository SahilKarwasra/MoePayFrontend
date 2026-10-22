package com.laarasoft.moepay.onboarding.domain

/** Mirrors MoePay-Backend internal/username/normalize.go. */
object UsernameValidator {
    const val MIN_LENGTH = 3
    const val MAX_LENGTH = 30

    private val pattern = Regex("^[a-z][a-z0-9_.]{2,29}$")

    fun normalize(raw: String): String = raw.trim().removePrefix("@").trim().lowercase()

    fun isValid(raw: String): Boolean {
        val normalized = normalize(raw)
        return pattern.matches(normalized) &&
            !normalized.contains("..") &&
            !normalized.endsWith(".")
    }
}
