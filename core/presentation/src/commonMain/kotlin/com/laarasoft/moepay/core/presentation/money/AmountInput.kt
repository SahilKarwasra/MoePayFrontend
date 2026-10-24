package com.laarasoft.moepay.core.presentation.money

/**
 * What the user has typed on the amount keypad, e.g. "12.5". Holds text rather than pence so
 * "12." and "12.50" render exactly as typed. Converts to pence without floating point.
 */
data class AmountInput(val text: String = "") {

    val pence: Long
        get() {
            if (text.isEmpty() || text == ".") return 0
            val pounds = text.substringBefore('.').ifEmpty { "0" }
            val fraction = text.substringAfter('.', "").padEnd(2, '0').take(2)
            return pounds.toLong() * 100 + fraction.toLong()
        }

    val isEmpty: Boolean get() = pence == 0L

    /** Grouped for display while typing: "1234.5" -> "1,234.5". */
    val display: String
        get() {
            if (text.isEmpty()) return "0"
            val pounds = text.substringBefore('.')
            val grouped = buildString {
                pounds.forEachIndexed { index, c ->
                    if (index > 0 && (pounds.length - index) % 3 == 0) append(',')
                    append(c)
                }
            }.ifEmpty { "0" }
            return if (text.contains('.')) grouped + "." + text.substringAfter('.') else grouped
        }

    /**
     * Applies a key press. Returns null when the key would make the amount invalid (more than
     * two decimals, a second point, a leading zero, or more than [maxPence]); the UI shakes.
     */
    fun press(key: AmountKey, maxPence: Long): AmountInput? = when (key) {
        AmountKey.Backspace -> AmountInput(text.dropLast(1))
        AmountKey.Decimal -> when {
            text.contains('.') -> null
            text.isEmpty() -> AmountInput("0.")
            else -> AmountInput("$text.")
        }
        is AmountKey.Digit -> {
            val next = when {
                text == "0" -> key.value.toString()
                else -> text + key.value
            }
            val decimals = next.substringAfter('.', "")
            when {
                text == "0" && key.value == 0 -> null
                next.contains('.') && decimals.length > 2 -> null
                next.substringBefore('.').length > 7 -> null
                AmountInput(next).pence > maxPence -> null
                else -> AmountInput(next)
            }
        }
    }

    companion object {
        fun fromPence(pence: Long): AmountInput {
            if (pence <= 0) return AmountInput()
            val pounds = pence / 100
            val fraction = pence % 100
            return AmountInput(if (fraction == 0L) "$pounds" else "$pounds." + fraction.toString().padStart(2, '0'))
        }
    }
}

sealed interface AmountKey {
    data class Digit(val value: Int) : AmountKey
    data object Decimal : AmountKey
    data object Backspace : AmountKey
}
