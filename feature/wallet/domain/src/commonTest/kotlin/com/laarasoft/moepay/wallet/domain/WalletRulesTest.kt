package com.laarasoft.moepay.wallet.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WalletRulesTest {
    @Test
    fun pinRulesMirrorTheBackend() {
        assertNull(PinRules.problem("482915"))
        assertEquals(PinProblem.FORMAT, PinRules.problem("12345"))
        assertEquals(PinProblem.FORMAT, PinRules.problem("12a456"))
        assertEquals(PinProblem.TOO_WEAK, PinRules.problem("111111"))
        assertEquals(PinProblem.TOO_WEAK, PinRules.problem("123456"))
        assertEquals(PinProblem.TOO_WEAK, PinRules.problem("654321"))
        assertEquals(PinProblem.TOO_WEAK, PinRules.problem("123123"))
    }

    @Test
    fun sortCodesAndAccountNumbers() {
        assertTrue(BankDetailsValidator.isValidSortCode("12-34-56"))
        assertTrue(BankDetailsValidator.isValidSortCode("123456"))
        assertFalse(BankDetailsValidator.isValidSortCode("12345"))
        assertEquals("12-34-56", BankDetailsValidator.formatSortCode("123456"))
        assertEquals("12-3", BankDetailsValidator.formatSortCode("123"))
        assertTrue(BankDetailsValidator.isValidAccountNumber("12345678"))
        assertFalse(BankDetailsValidator.isValidAccountNumber("1234567"))
    }

    @Test
    fun holderNames() {
        assertTrue(BankDetailsValidator.isValidHolderName("Alex O'Neil-Taylor"))
        assertTrue(BankDetailsValidator.isValidHolderName("Smith & Co."))
        assertFalse(BankDetailsValidator.isValidHolderName("Alex 2"))
        assertFalse(BankDetailsValidator.isValidHolderName("   "))
        assertEquals("Alex Taylor", BankDetailsValidator.cleanHolderName("  Alex   Taylor "))
    }
}
