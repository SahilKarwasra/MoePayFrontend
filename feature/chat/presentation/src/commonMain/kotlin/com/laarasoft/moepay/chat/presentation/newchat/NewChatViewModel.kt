package com.laarasoft.moepay.chat.presentation.newchat

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.chat.domain.ChatConnection
import com.laarasoft.moepay.chat.domain.ChatRepository
import com.laarasoft.moepay.core.domain.money.RecipientQuery
import com.laarasoft.moepay.core.domain.money.RecipientRef
import com.laarasoft.moepay.core.domain.util.onFailure
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.presentation.util.formatPhoneForDisplay
import com.laarasoft.moepay.core.presentation.util.toUiText
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

/** Who to open a chat with straight away (from a link or another screen), if anyone. */
data class NewChatTarget(val username: String? = null, val userId: String? = null)

class NewChatViewModel(
    private val target: NewChatTarget,
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private val eventChannel = Channel<NewChatEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(NewChatState())
    val state = combine(_state, chatRepository.observeConversations(), chatRepository.connection) { state, conversations, connection ->
        val q = state.queryState.text.toString().trim().removePrefix("@").lowercase()
        state.copy(
            recent = conversations.filter {
                q.isEmpty() || it.peer.displayName.lowercase().contains(q) || it.peer.username?.contains(q) == true
            },
            connection = connection,
        )
    }
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                observeQuery()
                openGivenTarget()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), NewChatState())

    fun onAction(action: NewChatAction) {
        when (action) {
            NewChatAction.OnBack -> send(NewChatEvent.Back)
            NewChatAction.OnStartChat -> start()
            is NewChatAction.OnConversationClick -> send(NewChatEvent.OpenConversation(action.conversationId))
        }
    }

    private fun observeQuery() {
        snapshotFlow { _state.value.queryState.text.toString() }
            .onEach { text ->
                val target = RecipientQuery.parse(text)
                _state.update {
                    it.copy(
                        target = target,
                        targetLabel = when (target) {
                            is RecipientRef.ByUsername -> "@${target.username}"
                            is RecipientRef.ByPhone -> formatPhoneForDisplay(target.phone)
                            else -> ""
                        },
                        error = null,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun start() {
        val current = state.value
        val target = current.target ?: return
        if (current.isOpening) return
        if (current.connection == ChatConnection.OFFLINE) return
        _state.update { it.copy(isOpening = true, error = null) }
        viewModelScope.launch {
            chatRepository.openConversation(target)
                .onSuccess { conversation -> send(NewChatEvent.OpenConversation(conversation.id)) }
                .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
            _state.update { it.copy(isOpening = false) }
        }
    }

    private fun openGivenTarget() {
        val ref = when {
            target.userId != null -> RecipientRef.ByUserId(target.userId)
            target.username != null -> RecipientRef.ByUsername(target.username.removePrefix("@"))
            else -> return
        }
        if (target.username != null) _state.value.queryState.setTextAndPlaceCursorAtEnd("@${target.username.removePrefix("@")}")
        _state.update { it.copy(isOpening = true) }
        viewModelScope.launch {
            chatRepository.openConversation(ref)
                .onSuccess { conversation -> send(NewChatEvent.OpenConversation(conversation.id)) }
                .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
            _state.update { it.copy(isOpening = false) }
        }
    }

    private fun send(event: NewChatEvent) {
        viewModelScope.launch { eventChannel.send(event) }
    }
}
