package com.laarasoft.moepay.core.domain.money

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RecipientQueryTest {
    @Test
    fun usernames() {
        assertEquals(RecipientRef.ByUsername("alex"), RecipientQuery.parse("@alex"))
        assertEquals(RecipientRef.ByUsername("alex.t"), RecipientQuery.parse("Alex.T"))
        assertNull(RecipientQuery.parse("al"))
        assertNull(RecipientQuery.parse("@1alex"))
    }

    @Test
    fun ukMobiles() {
        val expected = RecipientRef.ByPhone("+447911123456")
        assertEquals(expected, RecipientQuery.parse("07911 123456"))
        assertEquals(expected, RecipientQuery.parse("+44 7911 123456"))
        assertEquals(expected, RecipientQuery.parse("7911123456"))
        assertNull(RecipientQuery.parse("0791112"))
    }
}
