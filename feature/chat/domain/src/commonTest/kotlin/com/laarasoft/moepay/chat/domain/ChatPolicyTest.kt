package com.laarasoft.moepay.chat.domain

import com.laarasoft.moepay.core.domain.money.Money
import com.laarasoft.moepay.core.domain.money.MoneyRequestStatus
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class ChatPolicyTest {
    private val sentAt = Instant.parse("2026-10-26T10:00:00Z")

    private fun message(
        mine: Boolean = true,
        content: MessageContent = MessageContent.Text("hi"),
        serverId: String? = "m1",
        delivery: Delivery = Delivery.SENT,
    ) = ChatMessage(
        localId = "l1", serverId = serverId, conversationId = "c1", seq = 1, senderId = "u1", isMine = mine,
        content = content, replyToId = null, version = 1, isEdited = false, createdAt = sentAt, delivery = delivery,
    )

    @Test
    fun ownTextCanBeEditedWithinFifteenMinutes() {
        assertTrue(ChatPolicy.canEdit(message(), sentAt + 14.minutes))
        assertFalse(ChatPolicy.canEdit(message(), sentAt + 16.minutes))
    }

    @Test
    fun ownTextCanBeDeletedWithinFortyEightHours() {
        assertTrue(ChatPolicy.canDelete(message(), sentAt + 47.hours))
        assertFalse(ChatPolicy.canDelete(message(), sentAt + 49.hours))
    }

    @Test
    fun othersMessagesAndMoneyCardsCannotBeChanged() {
        assertFalse(ChatPolicy.canEdit(message(mine = false), sentAt))
        val request = MessageContent.Request("r1", Money(500, "£5.00"), true, MoneyRequestStatus.PENDING, emptySet(), null, null)
        assertFalse(ChatPolicy.canEdit(message(content = request), sentAt))
        assertFalse(ChatPolicy.canDelete(message(content = request), sentAt))
    }

    @Test
    fun pendingMessagesCannotBeEditedOrRepliedTo() {
        val pending = message(serverId = null, delivery = Delivery.PENDING)
        assertFalse(ChatPolicy.canEdit(pending, sentAt))
        assertFalse(ChatPolicy.canReply(pending))
        assertTrue(ChatPolicy.canReply(message()))
        assertFalse(ChatPolicy.canReply(message(content = MessageContent.Deleted)))
    }
}
