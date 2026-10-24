package com.laarasoft.moepay.core.domain.money

/**
 * An amount in GBP. Amounts move as integer pence; [display] is the server's ready-to-show
 * string (e.g. "£1,234.56") so the UI never re-formats money it received.
 */
data class Money(
    val pence: Long,
    val display: String,
) {
    companion object {
        val Zero = Money(0, "£0.00")
    }
}
