package com.laarasoft.moepay.chat.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Client-side copy of the backend's chat rules, used to decide which actions to offer.
 * The server still enforces them (`MESSAGE_NOT_EDITABLE`).
 */
object ChatPolicy {
    const val MAX_MESSAGE_LENGTH = 4000
    val EDIT_WINDOW: Duration = 15.minutes
    val DELETE_WINDOW: Duration = 48.hours

    fun canEdit(message: ChatMessage, now: Instant): Boolean =
        message.isMine && message.isSent && message.content is MessageContent.Text &&
            now - message.createdAt <= EDIT_WINDOW

    fun canDelete(message: ChatMessage, now: Instant): Boolean =
        message.isMine && message.isSent && message.content is MessageContent.Text &&
            now - message.createdAt <= DELETE_WINDOW

    /** Replies point at a server id, so only messages the server knows can be replied to. */
    fun canReply(message: ChatMessage): Boolean =
        message.isSent && message.content !is MessageContent.Deleted
}
