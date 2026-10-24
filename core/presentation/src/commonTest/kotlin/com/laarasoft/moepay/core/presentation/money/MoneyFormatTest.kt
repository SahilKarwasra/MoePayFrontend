package com.laarasoft.moepay.core.presentation.money

import kotlin.test.Test
import kotlin.test.assertEquals

/** Must match MoePay-Backend `money.Format` so count-up animations land on the server's text. */
class MoneyFormatTest {
    @Test
    fun formatsLikeTheBackend() {
        assertEquals("£0.00", formatPence(0))
        assertEquals("£0.05", formatPence(5))
        assertEquals("£12.50", formatPence(1250))
        assertEquals("£1,234.56", formatPence(123_456))
        assertEquals("£10,000.00", formatPence(1_000_000))
        assertEquals("-£5.00", formatPence(-500))
    }
}
