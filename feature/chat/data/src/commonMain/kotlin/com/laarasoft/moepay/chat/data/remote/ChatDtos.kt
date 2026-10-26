package com.laarasoft.moepay.chat.data.remote

import com.laarasoft.moepay.core.data.money.MoneyDto
import com.laarasoft.moepay.core.data.money.RecipientRefDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Mirrors MoePay-Backend internal/chat/views.go. Every endpoint, the sync feed and the
// realtime events share these shapes, so a client can upsert any of them by id.

@Serializable
data class PeerDto(
    @SerialName("user_id") val userId: String,
    val username: String? = null,
    @SerialName("display_name") val displayName: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

@Serializable
data class PaymentInfoDto(
    @SerialName("transaction_id") val transactionId: String,
    val reference: String = "",
    val amount: MoneyDto,
    /** "out" if the viewer paid, else "in". */
    val direction: String,
    val status: String,
)

@Serializable
data class RequestInfoDto(
    @SerialName("money_request_id") val moneyRequestId: String,
    val amount: MoneyDto,
    /** "outgoing" if the viewer asked, else "incoming". */
    val direction: String,
    val status: String,
    val actions: List<String> = emptyList(),
    @SerialName("expires_at") val expiresAt: String? = null,
)

@Serializable
data class MessageDto(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    val seq: Long,
    @SerialName("client_message_id") val clientMessageId: String? = null,
    @SerialName("sender_id") val senderId: String,
    val mine: Boolean,
    val kind: String,
    val body: String? = null,
    @SerialName("reply_to_id") val replyToId: String? = null,
    val payment: PaymentInfoDto? = null,
    val request: RequestInfoDto? = null,
    val version: Int = 1,
    val edited: Boolean = false,
    val deleted: Boolean = false,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class ConversationDto(
    val id: String,
    val peer: PeerDto,
    @SerialName("last_seq") val lastSeq: Long = 0,
    @SerialName("last_message") val lastMessage: MessageDto? = null,
    @SerialName("last_message_at") val lastMessageAt: String,
    @SerialName("unread_count") val unreadCount: Int = 0,
    @SerialName("my_last_read_seq") val myLastReadSeq: Long = 0,
    @SerialName("peer_last_read_seq") val peerLastReadSeq: Long = 0,
)

@Serializable
data class SyncItemDto(
    val seq: Long,
    /** "conversation" or "message". */
    val type: String,
    val conversation: ConversationDto? = null,
    val message: MessageDto? = null,
)

@Serializable
data class SyncPageDto(
    val items: List<SyncItemDto> = emptyList(),
    @SerialName("next_after") val nextAfter: Long,
    @SerialName("has_more") val hasMore: Boolean = false,
    @SerialName("latest_seq") val latestSeq: Long = 0,
)

@Serializable
data class MessagePageDto(
    val items: List<MessageDto> = emptyList(),
    @SerialName("next_before_seq") val nextBeforeSeq: Long? = null,
    @SerialName("has_more") val hasMore: Boolean = false,
)

@Serializable
data class OpenConversationRequest(val with: RecipientRefDto)

@Serializable
data class SendMessageRequest(
    @SerialName("client_message_id") val clientMessageId: String,
    val body: String,
    @SerialName("reply_to_id") val replyToId: String? = null,
)

@Serializable
data class EditMessageRequest(
    val body: String,
    @SerialName("base_version") val baseVersion: Int? = null,
)

@Serializable
data class ReadRequest(@SerialName("up_to_seq") val upToSeq: Long)

@Serializable
data class ChatPaymentRequest(
    @SerialName("client_message_id") val clientMessageId: String,
    @SerialName("amount_pence") val amountPence: Long,
    val note: String? = null,
    val pin: String,
)

@Serializable
data class ChatMoneyRequestRequest(
    @SerialName("client_message_id") val clientMessageId: String,
    @SerialName("amount_pence") val amountPence: Long,
    val note: String? = null,
)

/** Pay and request responses carry more; only the chat message matters here. */
@Serializable
data class MessageResultDto(val message: MessageDto)

@Serializable
data class TypingEventDto(
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("user_id") val userId: String = "",
    val typing: Boolean,
)
