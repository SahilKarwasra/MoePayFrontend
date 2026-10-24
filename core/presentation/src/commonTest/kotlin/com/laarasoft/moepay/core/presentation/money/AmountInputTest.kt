package com.laarasoft.moepay.core.presentation.money

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AmountInputTest {

    private fun type(vararg keys: AmountKey, max: Long = 500_000): AmountInput? =
        keys.fold(AmountInput() as AmountInput?) { acc, key -> acc?.press(key, max) }

    private fun digits(text: String) = text.map { if (it == '.') AmountKey.Decimal else AmountKey.Digit(it.digitToInt()) }.toTypedArray()

    @Test
    fun convertsToPenceWithoutFloatingPoint() {
        assertEquals(1250L, AmountInput("12.5").pence)
        assertEquals(1205L, AmountInput("12.05").pence)
        assertEquals(1200L, AmountInput("12.").pence)
        assertEquals(1L, AmountInput("0.01").pence)
        assertEquals(0L, AmountInput("").pence)
    }

    @Test
    fun displayGroupsThousandsAndKeepsTypedDecimals() {
        assertEquals("1,234.5", AmountInput("1234.5").display)
        assertEquals("0", AmountInput("").display)
        assertEquals("12.", AmountInput("12.").display)
    }

    @Test
    fun rejectsAThirdDecimal() {
        assertNull(type(*digits("1.234")))
        assertEquals(123L, type(*digits("1.23"))?.pence)
    }

    @Test
    fun rejectsASecondPointAndLeadingZeros() {
        assertNull(type(*digits("1.2.")))
        assertNull(type(AmountKey.Digit(0), AmountKey.Digit(0)))
        assertEquals("0.", type(AmountKey.Decimal)?.text)
    }

    @Test
    fun capsAtTheMaximum() {
        assertNull(type(*digits("5001"), max = 500_000))
        assertEquals(500_000L, type(*digits("5000"), max = 500_000)?.pence)
    }

    @Test
    fun backspaceAndRoundTrip() {
        assertEquals("12", AmountInput("12.").press(AmountKey.Backspace, 500_000)?.text)
        assertEquals("12.50", AmountInput.fromPence(1250).text)
        assertEquals("12.05", AmountInput.fromPence(1205).text)
        assertEquals("12", AmountInput.fromPence(1200).text)
    }
}
