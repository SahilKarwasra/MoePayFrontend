package com.laarasoft.moepay.chat.database

import com.laarasoft.moepay.chat.database.entity.ConversationEntity
import com.laarasoft.moepay.chat.database.entity.MessageEntity
import com.laarasoft.moepay.chat.database.entity.OutboxEntity
import com.laarasoft.moepay.chat.domain.ChatMessage
import com.laarasoft.moepay.chat.domain.Conversation
import com.laarasoft.moepay.chat.domain.Delivery
import com.laarasoft.moepay.chat.domain.MessageContent
import com.laarasoft.moepay.chat.domain.OutboxOperation
import com.laarasoft.moepay.chat.domain.OutboxType
import com.laarasoft.moepay.chat.domain.Peer
import com.laarasoft.moepay.core.domain.money.Money
import com.laarasoft.moepay.core.domain.money.MoneyRequestAction
import com.laarasoft.moepay.core.domain.money.MoneyRequestStatus
import com.laarasoft.moepay.core.domain.money.TransactionStatus
import com.laarasoft.moepay.core.domain.util.DataError
import kotlin.time.Instant

private const val KIND_TEXT = "text"
private const val KIND_PAYMENT = "payment"
private const val KIND_REQUEST = "request"

internal fun ConversationEntity.toDomain(lastMessage: ChatMessage?): Conversation {
    val serverActivity = Instant.fromEpochMilliseconds(lastMessageAt)
    return Conversation(
        id = id,
        peer = Peer(userId = peerUserId, username = peerUsername, displayName = peerDisplayName, avatarUrl = peerAvatarUrl),
        lastSeq = lastSeq,
        lastMessage = lastMessage,
        // A message still waiting to be sent counts as activity too.
        lastActivityAt = maxOf(serverActivity, lastMessage?.createdAt ?: serverActivity),
        unreadCount = unreadCount,
        myLastReadSeq = myLastReadSeq,
        peerLastReadSeq = peerLastReadSeq,
        draft = draft,
    )
}

internal fun Conversation.toEntity(draft: String): ConversationEntity = ConversationEntity(
    id = id,
    peerUserId = peer.userId,
    peerUsername = peer.username,
    peerDisplayName = peer.displayName,
    peerAvatarUrl = peer.avatarUrl,
    lastSeq = lastSeq,
    lastMessageAt = lastActivityAt.toEpochMilliseconds(),
    unreadCount = unreadCount,
    myLastReadSeq = myLastReadSeq,
    peerLastReadSeq = peerLastReadSeq,
    draft = draft,
)

internal fun MessageEntity.toDomain(): ChatMessage = ChatMessage(
    localId = localId,
    serverId = serverId,
    conversationId = conversationId,
    seq = seq,
    senderId = senderId,
    isMine = mine,
    content = when {
        deleted -> MessageContent.Deleted
        kind == KIND_PAYMENT -> MessageContent.Payment(
            transactionId = moneyId.orEmpty(),
            reference = reference.orEmpty(),
            amount = Money(amountPence ?: 0, amountDisplay.orEmpty()),
            isOutgoing = outgoing ?: mine,
            status = enumOrNull<TransactionStatus>(moneyStatus) ?: TransactionStatus.UNKNOWN,
            note = body,
        )
        kind == KIND_REQUEST -> MessageContent.Request(
            moneyRequestId = moneyId.orEmpty(),
            amount = Money(amountPence ?: 0, amountDisplay.orEmpty()),
            isOutgoing = outgoing ?: mine,
            status = enumOrNull<MoneyRequestStatus>(moneyStatus) ?: MoneyRequestStatus.UNKNOWN,
            actions = actions.orEmpty().split(',').mapNotNull { enumOrNull<MoneyRequestAction>(it) }.toSet(),
            expiresAt = expiresAt?.let(Instant::fromEpochMilliseconds),
            note = body,
        )
        else -> MessageContent.Text(body.orEmpty())
    },
    replyToId = replyToId,
    version = version,
    isEdited = edited,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    delivery = enumOrNull<Delivery>(delivery) ?: Delivery.SENT,
    failure = enumOrNull<DataError.Remote>(failure),
)

internal fun ChatMessage.toEntity(): MessageEntity {
    val payment = content as? MessageContent.Payment
    val request = content as? MessageContent.Request
    return MessageEntity(
        localId = localId,
        serverId = serverId,
        conversationId = conversationId,
        seq = seq,
        senderId = senderId,
        mine = isMine,
        kind = when (content) {
            is MessageContent.Payment -> KIND_PAYMENT
            is MessageContent.Request -> KIND_REQUEST
            else -> KIND_TEXT
        },
        body = when (val c = content) {
            is MessageContent.Text -> c.body
            is MessageContent.Payment -> c.note
            is MessageContent.Request -> c.note
            MessageContent.Deleted -> null
        },
        replyToId = replyToId,
        moneyId = payment?.transactionId ?: request?.moneyRequestId,
        reference = payment?.reference,
        amountPence = payment?.amount?.pence ?: request?.amount?.pence,
        amountDisplay = payment?.amount?.display ?: request?.amount?.display,
        outgoing = payment?.isOutgoing ?: request?.isOutgoing,
        moneyStatus = payment?.status?.name ?: request?.status?.name,
        actions = request?.actions?.joinToString(",") { it.name },
        expiresAt = request?.expiresAt?.toEpochMilliseconds(),
        version = version,
        edited = isEdited,
        deleted = content == MessageContent.Deleted,
        createdAt = createdAt.toEpochMilliseconds(),
        delivery = delivery.name,
        failure = failure?.name,
    )
}

internal fun OutboxEntity.toDomain(): OutboxOperation = OutboxOperation(
    id = id,
    type = OutboxType.valueOf(type),
    conversationId = conversationId,
    localId = localId,
    body = body,
    replyToId = replyToId,
    baseVersion = baseVersion,
    amountPence = amountPence,
    upToSeq = upToSeq,
    attempts = attempts,
    nextAttemptAt = Instant.fromEpochMilliseconds(nextAttemptAt),
    createdAt = Instant.fromEpochMilliseconds(createdAt),
)

internal fun OutboxOperation.toEntity(): OutboxEntity = OutboxEntity(
    id = id,
    type = type.name,
    conversationId = conversationId,
    localId = localId,
    body = body,
    replyToId = replyToId,
    baseVersion = baseVersion,
    amountPence = amountPence,
    upToSeq = upToSeq,
    attempts = attempts,
    nextAttemptAt = nextAttemptAt.toEpochMilliseconds(),
    createdAt = createdAt.toEpochMilliseconds(),
)

private inline fun <reified T : Enum<T>> enumOrNull(name: String?): T? =
    name?.let { value -> enumValues<T>().firstOrNull { it.name == value } }
