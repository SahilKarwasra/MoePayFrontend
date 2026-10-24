package com.laarasoft.moepay.wallet.domain

enum class PinProblem {
    FORMAT,
    TOO_WEAK,
}

/**
 * The backend's PIN rules (`internal/wallet/pin.go`), checked on the device so a weak PIN is
 * caught before the confirm step.
 */
object PinRules {
    const val LENGTH = 6

    private val weak = setOf("123123", "121212", "112233", "123321", "101010", "696969", "200000", "100000", "102030")

    fun problem(pin: String): PinProblem? = when {
        pin.length != LENGTH || !pin.all { it.isDigit() } -> PinProblem.FORMAT
        pin in weak || pin.all { it == pin[0] } || isSequence(pin, 1) || isSequence(pin, -1) -> PinProblem.TOO_WEAK
        else -> null
    }

    private fun isSequence(pin: String, step: Int): Boolean =
        (1 until pin.length).all { pin[it] - pin[it - 1] == step }
}
