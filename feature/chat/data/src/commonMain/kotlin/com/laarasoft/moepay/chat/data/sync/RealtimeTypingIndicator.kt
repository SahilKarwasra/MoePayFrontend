package com.laarasoft.moepay.chat.data.sync

import com.laarasoft.moepay.chat.domain.TypingIndicator
import com.laarasoft.moepay.core.domain.realtime.RealtimeEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private val SEND_EVERY = 3.seconds
private val IDLE_AFTER = 4.seconds
private val PEER_TIMEOUT = 6.seconds

/**
 * "Typing…" over the WebSocket. Outgoing signals are throttled and stop on their own when
 * the user pauses; incoming ones expire in case the "stopped" signal is lost.
 * All state is confined to the main thread.
 */
class RealtimeTypingIndicator(
    private val realtime: RealtimeEvents,
    private val json: Json,
    private val clock: Clock = Clock.System,
) : TypingIndicator {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _typingConversations = MutableStateFlow<Set<String>>(emptySet())
    override val typingConversations: StateFlow<Set<String>> = _typingConversations.asStateFlow()

    private val peerExpiry = mutableMapOf<String, Job>()
    private val lastSent = mutableMapOf<String, Instant>()
    private val idleTimers = mutableMapOf<String, Job>()

    /** From a `chat.typing` event. */
    fun onPeerTyping(conversationId: String, typing: Boolean) {
        scope.launch {
            peerExpiry.remove(conversationId)?.cancel()
            if (typing) {
                _typingConversations.update { it + conversationId }
                peerExpiry[conversationId] = scope.launch {
                    delay(PEER_TIMEOUT)
                    _typingConversations.update { it - conversationId }
                }
            } else {
                _typingConversations.update { it - conversationId }
            }
        }
    }

    override fun onUserTyping(conversationId: String) {
        val now = clock.now()
        val previous = lastSent[conversationId]
        if (previous == null || now - previous >= SEND_EVERY) {
            if (send(conversationId, typing = true)) lastSent[conversationId] = now
        }
        idleTimers.remove(conversationId)?.cancel()
        idleTimers[conversationId] = scope.launch {
            delay(IDLE_AFTER)
            onUserStoppedTyping(conversationId)
        }
    }

    override fun onUserStoppedTyping(conversationId: String) {
        idleTimers.remove(conversationId)?.cancel()
        if (lastSent.remove(conversationId) != null) send(conversationId, typing = false)
    }

    private fun send(conversationId: String, typing: Boolean): Boolean =
        realtime.send(
            type = "typing",
            dataJson = """{"conversation_id":${json.encodeToString(conversationId)},"typing":$typing}""",
        )
}
