package com.laarasoft.moepay.core.presentation.money

/** 123456 -> "£1,234.56"; matches the backend's `display` so animated values line up. */
fun formatPence(pence: Long, symbol: String = "£"): String {
    val negative = pence < 0
    val abs = if (negative) -pence else pence
    val pounds = (abs / 100).toString()
    val grouped = buildString {
        pounds.forEachIndexed { index, c ->
            if (index > 0 && (pounds.length - index) % 3 == 0) append(',')
            append(c)
        }
    }
    val fraction = (abs % 100).toString().padStart(2, '0')
    return (if (negative) "-" else "") + symbol + grouped + "." + fraction
}
