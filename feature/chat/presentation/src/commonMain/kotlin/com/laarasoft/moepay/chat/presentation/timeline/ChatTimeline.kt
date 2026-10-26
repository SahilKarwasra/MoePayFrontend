package com.laarasoft.moepay.chat.presentation.timeline

import com.laarasoft.moepay.chat.domain.ChatMessage
import com.laarasoft.moepay.chat.domain.Delivery
import com.laarasoft.moepay.chat.domain.MessageContent
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes

/** Where a bubble sits in a run of messages from the same person (rounder outer corners). */
enum class BubblePosition {
    SINGLE,
    FIRST,
    MIDDLE,
    LAST,
}

/** The tick next to my messages. */
enum class MessageStatus {
    NONE,
    PENDING,
    SENT,
    READ,
    FAILED,
}

/** A quoted message inside a reply bubble. */
data class ReplyPreview(
    val isMine: Boolean,
    /** Null when the original isn't on the device (shown as "Original message"). */
    val text: String?,
)

sealed interface TimelineItem {
    val key: String

    data class Message(
        val message: ChatMessage,
        val position: BubblePosition,
        val status: MessageStatus,
        val reply: ReplyPreview?,
    ) : TimelineItem {
        override val key: String get() = message.localId
    }

    data class DaySeparator(val date: LocalDate) : TimelineItem {
        override val key: String get() = "day-$date"
    }

    data object UnreadDivider : TimelineItem {
        override val key: String get() = "unread"
    }
}

private val RUN_GAP = 5.minutes

/**
 * Builds what the conversation list shows, newest first (it's drawn bottom-up):
 * bubbles grouped into runs, a date chip above each day, and an "Unread messages" divider
 * above the first message that was unread when the chat was opened.
 *
 * @param unreadAfterSeq the read pointer captured when the chat opened; null for no divider.
 */
fun buildTimeline(
    messages: List<ChatMessage>,
    peerLastReadSeq: Long,
    unreadAfterSeq: Long?,
    zone: TimeZone,
): List<TimelineItem> {
    if (messages.isEmpty()) return emptyList()
    val byServerId = messages.mapNotNull { m -> m.serverId?.let { it to m } }.toMap()
    val items = ArrayList<TimelineItem>(messages.size + 8)

    messages.forEachIndexed { index, message ->
        val newer = messages.getOrNull(index - 1)
        val older = messages.getOrNull(index + 1)
        val day = message.createdAt.toLocalDateTime(zone).date

        val joinsOlder = older != null && sameRun(older, message, zone)
        val joinsNewer = newer != null && sameRun(message, newer, zone)
        val position = when {
            joinsOlder && joinsNewer -> BubblePosition.MIDDLE
            joinsOlder -> BubblePosition.LAST
            joinsNewer -> BubblePosition.FIRST
            else -> BubblePosition.SINGLE
        }

        items += TimelineItem.Message(
            message = message,
            position = position,
            status = statusOf(message, peerLastReadSeq),
            reply = message.replyToId?.let { id ->
                val original = byServerId[id]
                ReplyPreview(isMine = original?.isMine ?: false, text = original?.let(::previewText))
            },
        )

        val seq = message.seq
        if (unreadAfterSeq != null && !message.isMine && seq != null && seq > unreadAfterSeq) {
            val olderSeq = older?.seq
            if (older == null || olderSeq == null || olderSeq <= unreadAfterSeq) items += TimelineItem.UnreadDivider
        }
        if (older == null || older.createdAt.toLocalDateTime(zone).date != day) {
            items += TimelineItem.DaySeparator(day)
        }
    }
    return items
}

private fun sameRun(older: ChatMessage, newer: ChatMessage, zone: TimeZone): Boolean =
    older.isMine == newer.isMine &&
        newer.createdAt - older.createdAt <= RUN_GAP &&
        older.createdAt.toLocalDateTime(zone).date == newer.createdAt.toLocalDateTime(zone).date

internal fun statusOf(message: ChatMessage, peerLastReadSeq: Long): MessageStatus {
    if (!message.isMine) return MessageStatus.NONE
    val seq = message.seq
    return when {
        message.delivery == Delivery.FAILED -> MessageStatus.FAILED
        message.delivery == Delivery.PENDING || seq == null -> MessageStatus.PENDING
        seq <= peerLastReadSeq -> MessageStatus.READ
        else -> MessageStatus.SENT
    }
}

/** One-line text of a message, for reply quotes. Money cards quote their amount. */
fun previewText(message: ChatMessage): String? = when (val content = message.content) {
    is MessageContent.Text -> content.body
    is MessageContent.Payment -> content.amount.display
    is MessageContent.Request -> content.amount.display
    MessageContent.Deleted -> null
}
