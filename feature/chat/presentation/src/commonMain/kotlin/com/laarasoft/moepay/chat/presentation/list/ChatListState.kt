package com.laarasoft.moepay.chat.presentation.list

import androidx.compose.foundation.text.input.TextFieldState
import com.laarasoft.moepay.chat.domain.ChatConnection
import com.laarasoft.moepay.chat.domain.Conversation

data class ChatListState(
    val conversations: List<Conversation> = emptyList(),
    val typingConversations: Set<String> = emptySet(),
    val searchState: TextFieldState = TextFieldState(),
    val query: String = "",
    /** True until the device database has answered and, when empty, the first sync ended. */
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val connection: ChatConnection = ChatConnection.ONLINE,
) {
    val visibleConversations: List<Conversation>
        get() {
            val q = query.trim().removePrefix("@").lowercase()
            if (q.isEmpty()) return conversations
            return conversations.filter {
                it.peer.displayName.lowercase().contains(q) || it.peer.username?.lowercase()?.contains(q) == true
            }
        }
}

sealed interface ChatListAction {
    data class OnConversationClick(val conversationId: String) : ChatListAction
    data object OnNewChatClick : ChatListAction
    data object OnRefresh : ChatListAction
}

sealed interface ChatListEvent {
    data class OpenConversation(val conversationId: String) : ChatListEvent
    data object OpenNewChat : ChatListEvent
}
