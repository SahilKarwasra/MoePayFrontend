package com.laarasoft.moepay.core.presentation.util

import com.laarasoft.moepay.core.presentation.resources.Res
import com.laarasoft.moepay.core.presentation.resources.date_today
import com.laarasoft.moepay.core.presentation.resources.date_yesterday
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.Instant

private val monthShort = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

fun Instant.toLocalDate(zone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    toLocalDateTime(zone).date

/** "14:05" in the device's time zone. */
fun Instant.formatTime(zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val t = toLocalDateTime(zone)
    return "${t.hour.toString().padStart(2, '0')}:${t.minute.toString().padStart(2, '0')}"
}

/** "12 Oct" this year, "12 Oct 2025" otherwise. */
fun LocalDate.formatShort(today: LocalDate): String {
    val base = "$day ${monthShort[month.number - 1]}"
    return if (year == today.year) base else "$base $year"
}

/** "12 Oct 2025, 14:05" */
fun Instant.formatDateTime(zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val date = toLocalDate(zone)
    return "${date.day} ${monthShort[date.month.number - 1]} ${date.year}, ${formatTime(zone)}"
}

/** Section header for a day: "Today", "Yesterday", "12 Oct". */
fun LocalDate.toDayLabel(
    today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
): UiText = when (this) {
    today -> UiText.Resource(Res.string.date_today)
    today.minus(DatePeriod(days = 1)) -> UiText.Resource(Res.string.date_yesterday)
    else -> UiText.DynamicString(formatShort(today))
}

/** Groups newest-first items into consecutive day sections, preserving order. */
fun <T> List<T>.groupByDay(
    zone: TimeZone = TimeZone.currentSystemDefault(),
    instantOf: (T) -> Instant,
): List<Pair<LocalDate, List<T>>> {
    val sections = mutableListOf<Pair<LocalDate, MutableList<T>>>()
    for (item in this) {
        val day = instantOf(item).toLocalDate(zone)
        val last = sections.lastOrNull()
        if (last != null && last.first == day) last.second += item else sections += day to mutableListOf(item)
    }
    return sections
}
