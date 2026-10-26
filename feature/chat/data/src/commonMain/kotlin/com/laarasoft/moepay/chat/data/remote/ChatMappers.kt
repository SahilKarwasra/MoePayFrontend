package com.laarasoft.moepay.chat.data.remote

import com.laarasoft.moepay.chat.domain.ChatMessage
import com.laarasoft.moepay.chat.domain.Conversation
import com.laarasoft.moepay.chat.domain.Delivery
import com.laarasoft.moepay.chat.domain.MessageContent
import com.laarasoft.moepay.chat.domain.Peer
import com.laarasoft.moepay.core.data.money.toDomain
import com.laarasoft.moepay.core.data.money.toInstant
import com.laarasoft.moepay.core.data.money.toInstantOrNull
import com.laarasoft.moepay.core.domain.money.MoneyRequestAction
import com.laarasoft.moepay.core.domain.money.MoneyRequestStatus
import com.laarasoft.moepay.core.domain.money.TransactionStatus

fun MessageDto.toDomain(): ChatMessage = ChatMessage(
    localId = clientMessageId ?: id,
    serverId = id,
    conversationId = conversationId,
    seq = seq,
    senderId = senderId,
    isMine = mine,
    content = when {
        deleted -> MessageContent.Deleted
        payment != null -> MessageContent.Payment(
            transactionId = payment.transactionId,
            reference = payment.reference,
            amount = payment.amount.toDomain(),
            isOutgoing = payment.direction == "out",
            status = when (payment.status) {
                "succeeded" -> TransactionStatus.SUCCEEDED
                "failed" -> TransactionStatus.FAILED
                else -> TransactionStatus.UNKNOWN
            },
            note = body,
        )
        request != null -> MessageContent.Request(
            moneyRequestId = request.moneyRequestId,
            amount = request.amount.toDomain(),
            isOutgoing = request.direction == "outgoing",
            status = when (request.status) {
                "pending" -> MoneyRequestStatus.PENDING
                "paid" -> MoneyRequestStatus.PAID
                "declined" -> MoneyRequestStatus.DECLINED
                "cancelled" -> MoneyRequestStatus.CANCELLED
                "expired" -> MoneyRequestStatus.EXPIRED
                else -> MoneyRequestStatus.UNKNOWN
            },
            actions = request.actions.mapNotNull { action ->
                when (action) {
                    "pay" -> MoneyRequestAction.PAY
                    "decline" -> MoneyRequestAction.DECLINE
                    "cancel" -> MoneyRequestAction.CANCEL
                    else -> null
                }
            }.toSet(),
            expiresAt = request.expiresAt.toInstantOrNull(),
            note = body,
        )
        else -> MessageContent.Text(body.orEmpty())
    },
    replyToId = replyToId,
    version = version,
    isEdited = edited,
    createdAt = createdAt.toInstant(),
    delivery = Delivery.SENT,
)

fun ConversationDto.toDomain(): Conversation = Conversation(
    id = id,
    peer = Peer(userId = peer.userId, username = peer.username, displayName = peer.displayName, avatarUrl = peer.avatarUrl),
    lastSeq = lastSeq,
    lastMessage = lastMessage?.toDomain(),
    lastActivityAt = lastMessageAt.toInstant(),
    unreadCount = unreadCount,
    myLastReadSeq = myLastReadSeq,
    peerLastReadSeq = peerLastReadSeq,
)
