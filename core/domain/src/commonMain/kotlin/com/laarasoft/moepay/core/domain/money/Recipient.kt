package com.laarasoft.moepay.core.domain.money

import kotlin.time.Instant

/** A payable MoePay user, as confirmed by the server before any money moves. */
data class Recipient(
    val userId: String,
    val username: String?,
    val displayName: String,
    val avatarUrl: String?,
    val phoneMasked: String,
)

data class RecentRecipient(
    val recipient: Recipient,
    val lastPaidAt: Instant,
)

/** How the user identified who they're paying or asking. Exactly one is sent to the server. */
sealed interface RecipientRef {
    data class ByUserId(val userId: String) : RecipientRef
    data class ByUsername(val username: String) : RecipientRef
    data class ByPhone(val phone: String) : RecipientRef
}
