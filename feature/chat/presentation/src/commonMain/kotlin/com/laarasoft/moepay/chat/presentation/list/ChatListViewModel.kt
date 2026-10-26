package com.laarasoft.moepay.chat.presentation.list

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.chat.domain.ChatRepository
import com.laarasoft.moepay.chat.domain.TypingIndicator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val chatRepository: ChatRepository,
    typingIndicator: TypingIndicator,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val eventChannel = Channel<ChatListEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ChatListState())
    val state = combine(
        _state,
        chatRepository.observeConversations(),
        typingIndicator.typingConversations,
        chatRepository.connection,
    ) { state, conversations, typing, connection ->
        state.copy(
            conversations = conversations,
            typingConversations = typing,
            connection = connection,
            isLoading = state.isLoading && conversations.isEmpty(),
        )
    }
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                observeSearch()
                viewModelScope.launch {
                    chatRepository.refresh()
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), ChatListState())

    fun onAction(action: ChatListAction) {
        when (action) {
            is ChatListAction.OnConversationClick -> send(ChatListEvent.OpenConversation(action.conversationId))
            ChatListAction.OnNewChatClick -> send(ChatListEvent.OpenNewChat)
            ChatListAction.OnRefresh -> viewModelScope.launch {
                _state.update { it.copy(isRefreshing = true) }
                chatRepository.refresh()
                _state.update { it.copy(isRefreshing = false, isLoading = false) }
            }
        }
    }

    private fun observeSearch() {
        snapshotFlow { _state.value.searchState.text.toString() }
            .onEach { query -> _state.update { it.copy(query = query) } }
            .launchIn(viewModelScope)
    }

    private fun send(event: ChatListEvent) {
        viewModelScope.launch { eventChannel.send(event) }
    }
}
