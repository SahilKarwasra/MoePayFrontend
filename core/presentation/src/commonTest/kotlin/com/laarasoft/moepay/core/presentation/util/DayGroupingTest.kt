package com.laarasoft.moepay.core.presentation.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class DayGroupingTest {
    @Test
    fun groupsConsecutiveDaysInOrder() {
        val items = listOf(
            "2026-10-23T18:00:00Z",
            "2026-10-23T09:00:00Z",
            "2026-10-22T23:30:00Z",
            "2026-10-20T12:00:00Z",
        ).map(Instant::parse)

        val sections = items.groupByDay(TimeZone.UTC) { it }

        assertEquals(
            listOf(LocalDate(2026, 10, 23), LocalDate(2026, 10, 22), LocalDate(2026, 10, 20)),
            sections.map { it.first },
        )
        assertEquals(listOf(2, 1, 1), sections.map { it.second.size })
    }

    @Test
    fun usesTheGivenTimeZone() {
        // 23:30 UTC on the 22nd is already the 23rd in London summer time.
        val sections = listOf(Instant.parse("2026-07-22T23:30:00Z")).groupByDay(TimeZone.of("Europe/London")) { it }
        assertEquals(LocalDate(2026, 7, 23), sections.single().first)
    }

    @Test
    fun formatsShortDates() {
        val today = LocalDate(2026, 10, 23)
        assertEquals("12 Oct", LocalDate(2026, 10, 12).formatShort(today))
        assertEquals("12 Oct 2025", LocalDate(2025, 10, 12).formatShort(today))
    }
}
