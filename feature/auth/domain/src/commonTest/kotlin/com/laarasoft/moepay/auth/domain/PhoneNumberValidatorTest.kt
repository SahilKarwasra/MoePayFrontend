package com.laarasoft.moepay.auth.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhoneNumberValidatorTest {
    @Test
    fun acceptsUkMobilesWithOrWithoutTheTrunkZero() {
        assertTrue(PhoneNumberValidator.isValid("07911123456"))
        assertTrue(PhoneNumberValidator.isValid("7911123456"))
        assertTrue(PhoneNumberValidator.isValid("07911 123456"))
    }

    @Test
    fun rejectsLandlinesAndWrongLengths() {
        assertFalse(PhoneNumberValidator.isValid("02079460000"))
        assertFalse(PhoneNumberValidator.isValid("0791112345"))
        assertFalse(PhoneNumberValidator.isValid(""))
    }

    @Test
    fun normalisesAndFormats() {
        assertEquals("+447911123456", PhoneNumberValidator.toE164("07911 123456"))
        assertEquals("+44 7911 123456", PhoneNumberValidator.format("+447911123456"))
        assertEquals("+15551234567", PhoneNumberValidator.format("+15551234567"))
    }
}
