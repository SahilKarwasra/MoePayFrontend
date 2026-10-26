package com.laarasoft.moepay.chat.presentation.newchat

import androidx.compose.foundation.text.input.TextFieldState
import com.laarasoft.moepay.chat.domain.ChatConnection
import com.laarasoft.moepay.chat.domain.Conversation
import com.laarasoft.moepay.core.domain.money.RecipientRef
import com.laarasoft.moepay.core.presentation.util.UiText

data class NewChatState(
    val queryState: TextFieldState = TextFieldState(),
    /** What the typed text resolves to, once it's a plausible @username or UK mobile. */
    val target: RecipientRef? = null,
    val targetLabel: String = "",
    val recent: List<Conversation> = emptyList(),
    val isOpening: Boolean = false,
    val error: UiText? = null,
    val connection: ChatConnection = ChatConnection.ONLINE,
)

sealed interface NewChatAction {
    data object OnBack : NewChatAction
    data object OnStartChat : NewChatAction
    data class OnConversationClick(val conversationId: String) : NewChatAction
}

sealed interface NewChatEvent {
    data object Back : NewChatEvent
    data class OpenConversation(val conversationId: String) : NewChatEvent
}
