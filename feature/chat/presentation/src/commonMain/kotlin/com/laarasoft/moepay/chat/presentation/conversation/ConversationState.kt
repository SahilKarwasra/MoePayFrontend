package com.laarasoft.moepay.chat.presentation.conversation

import androidx.compose.foundation.text.input.TextFieldState
import com.laarasoft.moepay.chat.domain.ChatConnection
import com.laarasoft.moepay.chat.domain.ChatMessage
import com.laarasoft.moepay.chat.presentation.timeline.TimelineItem

data class ConversationState(
    val conversationId: String,
    val peerName: String = "",
    val peerUsername: String? = null,
    val connection: ChatConnection = ChatConnection.ONLINE,
    val isPeerTyping: Boolean = false,
    val timeline: List<TimelineItem> = emptyList(),
    val hasLoaded: Boolean = false,
    val isLoadingOlder: Boolean = false,
    val canLoadOlder: Boolean = true,
    val composer: TextFieldState = TextFieldState(),
    val replyTo: ChatMessage? = null,
    val editing: ChatMessage? = null,
    /** The message whose long-press menu is open. */
    val menuFor: ChatMessage? = null,
    val confirmDeleteFor: ChatMessage? = null,
    val isMoneyMenuVisible: Boolean = false,
    val isOfflineMoneyNoticeVisible: Boolean = false,
)

sealed interface ConversationAction {
    data object OnBack : ConversationAction
    data class OnVisibilityChanged(val isVisible: Boolean) : ConversationAction
    data object OnSend : ConversationAction
    data object OnLoadOlder : ConversationAction
    data class OnMessageTap(val message: ChatMessage) : ConversationAction
    data class OnMessageLongPress(val message: ChatMessage) : ConversationAction
    data object OnDismissMenu : ConversationAction
    data class OnReply(val message: ChatMessage) : ConversationAction
    data object OnCancelReply : ConversationAction
    data class OnCopy(val message: ChatMessage) : ConversationAction
    data class OnEdit(val message: ChatMessage) : ConversationAction
    data object OnCancelEdit : ConversationAction
    data class OnDelete(val message: ChatMessage) : ConversationAction
    data object OnConfirmDelete : ConversationAction
    data object OnDismissDelete : ConversationAction
    data class OnRetry(val message: ChatMessage) : ConversationAction
    data class OnDiscard(val message: ChatMessage) : ConversationAction
    data class OnOpenRequest(val moneyRequestId: String) : ConversationAction
    data object OnMoneyClick : ConversationAction
    data object OnDismissMoneyMenu : ConversationAction
    data object OnSendMoneyClick : ConversationAction
    data object OnRequestMoneyClick : ConversationAction
    data object OnDismissOfflineMoneyNotice : ConversationAction
}

sealed interface ConversationEvent {
    data object Back : ConversationEvent
    data class OpenRequest(val moneyRequestId: String) : ConversationEvent
    data class OpenTransaction(val transactionId: String) : ConversationEvent
    data class Copy(val text: String) : ConversationEvent
    data class OpenMoney(val isSend: Boolean) : ConversationEvent
    /** My own message was added: scroll to it. */
    data object ScrollToLatest : ConversationEvent
}
