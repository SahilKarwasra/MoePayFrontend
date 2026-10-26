package com.laarasoft.moepay.chat.domain

import com.laarasoft.moepay.core.domain.money.Money
import com.laarasoft.moepay.core.domain.money.MoneyRequestAction
import com.laarasoft.moepay.core.domain.money.MoneyRequestStatus
import com.laarasoft.moepay.core.domain.money.TransactionStatus
import com.laarasoft.moepay.core.domain.util.DataError
import kotlin.time.Instant

/** Where an outgoing message is on its way to the server. Messages from others are [SENT]. */
enum class Delivery {
    /** Saved on the device, waiting to be sent (or being sent). */
    PENDING,
    SENT,

    /** The server refused it; the user can retry or discard it. */
    FAILED,
}

sealed interface MessageContent {
    data class Text(val body: String) : MessageContent

    data class Payment(
        val transactionId: String,
        val reference: String,
        val amount: Money,
        /** True when the viewer paid. */
        val isOutgoing: Boolean,
        val status: TransactionStatus,
        val note: String?,
    ) : MessageContent

    data class Request(
        val moneyRequestId: String,
        val amount: Money,
        /** True when the viewer asked for the money. */
        val isOutgoing: Boolean,
        val status: MoneyRequestStatus,
        /** Server-decided: what the viewer may do now. */
        val actions: Set<MoneyRequestAction>,
        val expiresAt: Instant?,
        val note: String?,
    ) : MessageContent

    /** Deleted for everyone. */
    data object Deleted : MessageContent
}

data class ChatMessage(
    /**
     * Stable local key: the `client_message_id` when there is one (every message typed on a
     * device), otherwise the server id (e.g. payments made from the wallet screen). It never
     * changes, so a pending bubble becomes the sent one in place.
     */
    val localId: String,
    val serverId: String?,
    val conversationId: String,
    /** Per-conversation order assigned by the server; null until the server accepts it. */
    val seq: Long?,
    val senderId: String,
    val isMine: Boolean,
    val content: MessageContent,
    /** Server id of the message this one replies to. */
    val replyToId: String?,
    val version: Int,
    val isEdited: Boolean,
    val createdAt: Instant,
    val delivery: Delivery,
    /** Why the server refused it, when [delivery] is [Delivery.FAILED]. */
    val failure: DataError.Remote? = null,
) {
    val isSent: Boolean get() = delivery == Delivery.SENT && serverId != null
}
