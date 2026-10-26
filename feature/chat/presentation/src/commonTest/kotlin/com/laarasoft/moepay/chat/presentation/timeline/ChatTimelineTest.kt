package com.laarasoft.moepay.chat.presentation.timeline

import com.laarasoft.moepay.chat.domain.ChatMessage
import com.laarasoft.moepay.chat.domain.Delivery
import com.laarasoft.moepay.chat.domain.MessageContent
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class ChatTimelineTest {
    private val zone = TimeZone.UTC
    private val t0 = Instant.parse("2026-10-26T09:00:00Z")

    private fun msg(seq: Long?, mine: Boolean, at: Instant, delivery: Delivery = Delivery.SENT, replyTo: String? = null) = ChatMessage(
        localId = "l$seq-$at", serverId = seq?.let { "s$it" }, conversationId = "c", seq = seq, senderId = if (mine) "me" else "them",
        isMine = mine, content = MessageContent.Text("m$seq"), replyToId = replyTo, version = 1, isEdited = false,
        createdAt = at, delivery = delivery,
    )

    private fun messagesOf(items: List<TimelineItem>) = items.filterIsInstance<TimelineItem.Message>()

    @Test
    fun consecutiveMessagesFromOnePersonFormARun() {
        // Newest first: three of mine within minutes, then one of theirs.
        val list = listOf(
            msg(4, true, t0 + 3.minutes),
            msg(3, true, t0 + 2.minutes),
            msg(2, true, t0 + 1.minutes),
            msg(1, false, t0),
        )
        val positions = messagesOf(buildTimeline(list, peerLastReadSeq = 0, unreadAfterSeq = null, zone = zone)).map { it.position }
        assertEquals(listOf(BubblePosition.LAST, BubblePosition.MIDDLE, BubblePosition.FIRST, BubblePosition.SINGLE), positions)
    }

    @Test
    fun aLongPauseStartsANewRun() {
        val list = listOf(msg(2, true, t0 + 10.minutes), msg(1, true, t0))
        val positions = messagesOf(buildTimeline(list, 0, null, zone)).map { it.position }
        assertEquals(listOf(BubblePosition.SINGLE, BubblePosition.SINGLE), positions)
    }

    @Test
    fun dayChipsSitAboveEachDaysOldestMessage() {
        val list = listOf(msg(3, false, t0 + 26.hours), msg(2, false, t0 + 1.minutes), msg(1, false, t0))
        val items = buildTimeline(list, 0, null, zone)
        val days = items.filterIsInstance<TimelineItem.DaySeparator>().map { it.date }
        assertEquals(listOf(LocalDate(2026, 10, 27), LocalDate(2026, 10, 26)), days)
        // Reversed list: the chip follows (is drawn above) the oldest message of its day.
        assertEquals(TimelineItem.DaySeparator(LocalDate(2026, 10, 26)), items.last())
    }

    @Test
    fun unreadDividerSitsAboveTheFirstUnreadMessage() {
        val list = listOf(msg(4, false, t0 + 3.minutes), msg(3, false, t0 + 2.minutes), msg(2, true, t0 + 1.minutes), msg(1, false, t0))
        val items = buildTimeline(list, peerLastReadSeq = 0, unreadAfterSeq = 2, zone = zone)
        val dividerIndex = items.indexOf(TimelineItem.UnreadDivider)
        val firstUnread = items.indexOfFirst { it is TimelineItem.Message && it.message.seq == 3L }
        assertEquals(firstUnread + 1, dividerIndex)
    }

    @Test
    fun ticksFollowDeliveryAndTheReadPointer() {
        val list = listOf(
            msg(null, true, t0 + 3.minutes, Delivery.FAILED),
            msg(null, true, t0 + 2.minutes, Delivery.PENDING),
            msg(2, true, t0 + 1.minutes),
            msg(1, true, t0),
        )
        val statuses = messagesOf(buildTimeline(list, peerLastReadSeq = 1, unreadAfterSeq = null, zone = zone)).map { it.status }
        assertEquals(listOf(MessageStatus.FAILED, MessageStatus.PENDING, MessageStatus.SENT, MessageStatus.READ), statuses)
    }

    @Test
    fun repliesQuoteTheOriginalWhenItIsOnTheDevice() {
        val list = listOf(msg(3, true, t0 + 2.minutes, replyTo = "s1"), msg(2, true, t0 + 1.minutes, replyTo = "missing"), msg(1, false, t0))
        val replies = messagesOf(buildTimeline(list, 0, null, zone)).map { it.reply }
        assertEquals(ReplyPreview(isMine = false, text = "m1"), replies[0])
        assertEquals(ReplyPreview(isMine = false, text = null), replies[1])
    }
}
