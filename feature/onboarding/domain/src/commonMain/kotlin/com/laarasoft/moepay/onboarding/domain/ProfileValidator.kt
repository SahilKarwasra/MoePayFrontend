package com.laarasoft.moepay.onboarding.domain

object ProfileValidator {
    const val NAME_MAX_LENGTH = 50

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidName(name: String): Boolean {
        val trimmed = name.trim()
        return trimmed.isNotEmpty() && trimmed.length <= NAME_MAX_LENGTH
    }

    /** Email is optional: blank is valid. */
    fun isValidOptionalEmail(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.isEmpty() || emailRegex.matches(trimmed)
    }
}
