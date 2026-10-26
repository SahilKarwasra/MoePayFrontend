package com.laarasoft.moepay.chat.presentation.conversation

import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laarasoft.moepay.chat.domain.ChatConnection
import com.laarasoft.moepay.chat.domain.ChatMessage
import com.laarasoft.moepay.chat.domain.ChatPolicy
import com.laarasoft.moepay.chat.domain.ChatRepository
import com.laarasoft.moepay.chat.domain.Delivery
import com.laarasoft.moepay.chat.domain.MessageContent
import com.laarasoft.moepay.chat.domain.TypingIndicator
import com.laarasoft.moepay.chat.presentation.timeline.buildTimeline
import com.laarasoft.moepay.core.domain.util.onSuccess
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

private const val PAGE = 60

class ConversationViewModel(
    private val conversationId: String,
    private val chatRepository: ChatRepository,
    private val typingIndicator: TypingIndicator,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private var isVisible = false

    /** Read pointer when the chat opened; places the "Unread messages" divider. Null: none. */
    private var unreadAnchor: Long? = null
    private var anchorCaptured = false

    private val eventChannel = Channel<ConversationEvent>()
    val events = eventChannel.receiveAsFlow()

    private val limit = MutableStateFlow(PAGE)
    private val messages = limit.flatMapLatest { chatRepository.observeMessages(conversationId, it) }
    private val conversation = chatRepository.observeConversation(conversationId)

    private val _state = MutableStateFlow(ConversationState(conversationId = conversationId))
    val state = combine(
        _state,
        conversation,
        messages,
        typingIndicator.typingConversations,
        chatRepository.connection,
    ) { state, conversation, messages, typing, connection ->
        if (conversation != null && !anchorCaptured) {
            anchorCaptured = true
            unreadAnchor = conversation.myLastReadSeq.takeIf { conversation.unreadCount > 0 }
        }
        state.copy(
            peerName = conversation?.peer?.displayName ?: state.peerName,
            peerUsername = conversation?.peer?.username,
            connection = connection,
            isPeerTyping = conversationId in typing,
            timeline = buildTimeline(
                messages = messages,
                peerLastReadSeq = conversation?.peerLastReadSeq ?: 0,
                unreadAfterSeq = unreadAnchor,
                zone = TimeZone.currentSystemDefault(),
            ),
            hasLoaded = state.hasLoaded || conversation != null,
        )
    }
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                restoreDraft()
                observeComposer()
                observeIncoming()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), ConversationState(conversationId = conversationId))

    fun onAction(action: ConversationAction) {
        when (action) {
            ConversationAction.OnBack -> send(ConversationEvent.Back)
            is ConversationAction.OnVisibilityChanged -> {
                isVisible = action.isVisible
                if (isVisible) markLatestRead() else typingIndicator.onUserStoppedTyping(conversationId)
            }
            ConversationAction.OnSend -> submit()
            ConversationAction.OnLoadOlder -> loadOlder()
            is ConversationAction.OnMessageTap -> onTap(action.message)
            is ConversationAction.OnMessageLongPress -> if (action.message.content !is MessageContent.Deleted) {
                _state.update { it.copy(menuFor = action.message) }
            }
            ConversationAction.OnDismissMenu -> _state.update { it.copy(menuFor = null) }
            is ConversationAction.OnReply -> if (ChatPolicy.canReply(action.message)) {
                _state.update { it.copy(replyTo = action.message, editing = null, menuFor = null) }
            }
            ConversationAction.OnCancelReply -> _state.update { it.copy(replyTo = null) }
            is ConversationAction.OnCopy -> {
                _state.update { it.copy(menuFor = null) }
                (action.message.content as? MessageContent.Text)?.let { send(ConversationEvent.Copy(it.body)) }
            }
            is ConversationAction.OnEdit -> startEditing(action.message)
            ConversationAction.OnCancelEdit -> {
                _state.update { it.copy(editing = null) }
                _state.value.composer.clearText()
            }
            is ConversationAction.OnDelete -> _state.update { it.copy(confirmDeleteFor = action.message, menuFor = null) }
            ConversationAction.OnConfirmDelete -> {
                val message = _state.value.confirmDeleteFor ?: return
                _state.update { it.copy(confirmDeleteFor = null) }
                viewModelScope.launch { chatRepository.delete(message.localId) }
            }
            ConversationAction.OnDismissDelete -> _state.update { it.copy(confirmDeleteFor = null) }
            is ConversationAction.OnRetry -> {
                _state.update { it.copy(menuFor = null) }
                viewModelScope.launch { chatRepository.retry(action.message.localId) }
            }
            is ConversationAction.OnDiscard -> {
                _state.update { it.copy(menuFor = null) }
                viewModelScope.launch { chatRepository.discard(action.message.localId) }
            }
            is ConversationAction.OnOpenRequest -> send(ConversationEvent.OpenRequest(action.moneyRequestId))
            ConversationAction.OnMoneyClick -> _state.update { it.copy(isMoneyMenuVisible = true) }
            ConversationAction.OnDismissMoneyMenu -> _state.update { it.copy(isMoneyMenuVisible = false) }
            ConversationAction.OnSendMoneyClick -> {
                // Paying needs the PIN, which is never stored, so it can't wait in the outbox.
                if (_state.value.connection == ChatConnection.OFFLINE) {
                    _state.update { it.copy(isMoneyMenuVisible = false, isOfflineMoneyNoticeVisible = true) }
                } else {
                    _state.update { it.copy(isMoneyMenuVisible = false) }
                    send(ConversationEvent.OpenMoney(isSend = true))
                }
            }
            ConversationAction.OnRequestMoneyClick -> {
                _state.update { it.copy(isMoneyMenuVisible = false) }
                send(ConversationEvent.OpenMoney(isSend = false))
            }
            ConversationAction.OnDismissOfflineMoneyNotice -> _state.update { it.copy(isOfflineMoneyNoticeVisible = false) }
        }
    }

    private fun onTap(message: ChatMessage) {
        when (val content = message.content) {
            is MessageContent.Payment -> send(ConversationEvent.OpenTransaction(content.transactionId))
            is MessageContent.Request -> if (content.moneyRequestId.isNotEmpty()) send(ConversationEvent.OpenRequest(content.moneyRequestId))
            else -> if (message.delivery == Delivery.FAILED) _state.update { it.copy(menuFor = message) }
        }
    }

    private fun submit() {
        val state = _state.value
        val text = state.composer.text.toString().trim()
        if (text.isEmpty()) return
        val editing = state.editing
        val replyTo = state.replyTo
        state.composer.clearText()
        _state.update { it.copy(editing = null, replyTo = null) }
        typingIndicator.onUserStoppedTyping(conversationId)
        viewModelScope.launch {
            if (editing != null) {
                chatRepository.edit(editing.localId, text)
            } else {
                chatRepository.sendText(conversationId, text, replyTo?.serverId)
                eventChannel.send(ConversationEvent.ScrollToLatest)
            }
        }
    }

    private fun startEditing(message: ChatMessage) {
        val body = (message.content as? MessageContent.Text)?.body ?: return
        if (!ChatPolicy.canEdit(message, Clock.System.now())) return
        _state.update { it.copy(editing = message, replyTo = null, menuFor = null) }
        _state.value.composer.setTextAndPlaceCursorAtEnd(body)
    }

    private fun loadOlder() {
        val state = _state.value
        if (state.isLoadingOlder || !state.canLoadOlder) return
        _state.update { it.copy(isLoadingOlder = true) }
        viewModelScope.launch {
            val shown = limit.value
            limit.value = shown + PAGE
            // When the device has fewer messages than we're about to show, ask the server.
            val onDevice = chatRepository.observeMessages(conversationId, shown + PAGE).first().size
            var more = true
            if (onDevice < shown + PAGE) {
                chatRepository.loadOlder(conversationId).onSuccess { more = it }
            }
            _state.update { it.copy(isLoadingOlder = false, canLoadOlder = more || onDevice >= shown + PAGE) }
        }
    }

    private suspend fun restoreDraft() {
        val draft = chatRepository.observeConversation(conversationId).filterNotNull().first().draft
        if (draft.isNotEmpty() && _state.value.composer.text.isEmpty()) {
            _state.value.composer.setTextAndPlaceCursorAtEnd(draft)
        }
    }

    private fun observeComposer() {
        val composerText = snapshotFlow { _state.value.composer.text.toString() }.distinctUntilChanged()
        composerText.drop(1).onEach { text ->
            if (_state.value.editing != null) return@onEach
            if (text.isBlank()) typingIndicator.onUserStoppedTyping(conversationId) else typingIndicator.onUserTyping(conversationId)
        }.launchIn(viewModelScope)
        composerText.debounce(400).onEach { text ->
            if (_state.value.editing == null) chatRepository.saveDraft(conversationId, text)
        }.launchIn(viewModelScope)
    }

    /** Marks new messages read while the chat is on screen. */
    private fun observeIncoming() {
        messages.onEach { if (isVisible) markLatestRead() }.launchIn(viewModelScope)
    }

    private fun markLatestRead() {
        viewModelScope.launch {
            // The newest rows may still be pending (no seq yet).
            val latest = chatRepository.observeMessages(conversationId, 20).first().firstNotNullOfOrNull { it.seq } ?: return@launch
            chatRepository.markRead(conversationId, latest)
        }
    }

    private fun send(event: ConversationEvent) {
        viewModelScope.launch { eventChannel.send(event) }
    }
}
