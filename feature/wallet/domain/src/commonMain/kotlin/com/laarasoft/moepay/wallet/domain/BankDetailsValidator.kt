package com.laarasoft.moepay.wallet.domain

/** Mirrors MoePay-Backend `internal/bank` validation for UK accounts. */
object BankDetailsValidator {
    const val SORT_CODE_DIGITS = 6
    const val ACCOUNT_NUMBER_DIGITS = 8
    const val HOLDER_NAME_MAX = 70

    private val holderPattern = Regex("^[\\p{L} .'&-]+$")

    fun digits(raw: String): String = raw.filter { it.isDigit() }

    fun isValidSortCode(raw: String): Boolean = digits(raw).length == SORT_CODE_DIGITS

    fun isValidAccountNumber(raw: String): Boolean = digits(raw).length == ACCOUNT_NUMBER_DIGITS

    fun cleanHolderName(raw: String): String = raw.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString(" ")

    fun isValidHolderName(raw: String): Boolean {
        val name = cleanHolderName(raw)
        return name.isNotEmpty() && name.length <= HOLDER_NAME_MAX && holderPattern.matches(name)
    }

    /** "123456" -> "12-34-56" (partial input is grouped as far as it goes). */
    fun formatSortCode(raw: String): String = digits(raw).take(SORT_CODE_DIGITS).chunked(2).joinToString("-")
}
