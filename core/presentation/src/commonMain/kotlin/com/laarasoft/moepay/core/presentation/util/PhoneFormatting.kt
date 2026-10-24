package com.laarasoft.moepay.core.presentation.util

/** "+447911123456" -> "+44 7911 123456"; other numbers are returned unchanged. */
fun formatPhoneForDisplay(e164: String): String {
    if (!e164.startsWith("+44") || e164.length != 13) return e164
    val national = e164.drop(3)
    return "+44 ${national.take(4)} ${national.drop(4)}"
}
