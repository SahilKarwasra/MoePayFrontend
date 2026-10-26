package com.laarasoft.moepay.chat.data.sync

import co.touchlab.kermit.Logger
import com.laarasoft.moepay.chat.data.remote.SyncPageDto
import com.laarasoft.moepay.chat.data.remote.TypingEventDto
import com.laarasoft.moepay.chat.domain.ChatEngine
import com.laarasoft.moepay.chat.domain.ChatLocalDataSource
import com.laarasoft.moepay.core.data.di.ApplicationScope
import com.laarasoft.moepay.core.domain.connectivity.ConnectivityObserver
import com.laarasoft.moepay.core.domain.realtime.RealtimeConnection
import com.laarasoft.moepay.core.domain.realtime.RealtimeEvent
import com.laarasoft.moepay.core.domain.realtime.RealtimeEvents
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlin.time.Clock

/**
 * Wires sync and the outbox to everything that should wake them: app start, the live
 * connection coming up, the network coming back, pushed `chat.changes`, and new local writes.
 */
class DefaultChatEngine(
    private val sync: ChatSyncEngine,
    private val outbox: OutboxProcessor,
    private val local: ChatLocalDataSource,
    private val realtime: RealtimeEvents,
    private val connectivity: ConnectivityObserver,
    private val typing: RealtimeTypingIndicator,
    private val json: Json,
    private val applicationScope: ApplicationScope,
    private val clock: Clock = Clock.System,
) : ChatEngine {

    private val logger = Logger.withTag("MoePayChat")
    private val wake = Channel<Unit>(Channel.CONFLATED)
    private var job: Job? = null

    /** A local write is waiting in the outbox. */
    fun wakeOutbox() {
        wake.trySend(Unit)
    }

    override fun start() {
        if (job?.isActive == true) return
        job = applicationScope.launch {
            launch { outboxLoop() }
            launch {
                realtime.connection.drop(1).filter { it == RealtimeConnection.CONNECTED }.collect {
                    // Anything pushed while disconnected was missed: catch up.
                    pull()
                    wakeOutbox()
                }
            }
            launch {
                connectivity.isOnline.drop(1).filter { it }.collect {
                    wakeOutbox()
                    pull()
                }
            }
            launch {
                realtime.events.filterIsInstance<RealtimeEvent.Other>().collect { handle(it) }
            }
            pull()
        }
    }

    override fun stop() {
        job?.cancel()
        job = null
    }

    override suspend fun clear() {
        stop()
        local.clear()
    }

    private suspend fun pull() {
        sync.pull()
    }

    private suspend fun outboxLoop() {
        while (currentCoroutineContext().isActive) {
            val nextAttempt = runCatching { outbox.drain() }
                .onFailure { logger.w { "outbox failed: ${it.message}" } }
                .getOrNull()
            if (nextAttempt == null) {
                wake.receive()
            } else {
                withTimeoutOrNull((nextAttempt - clock.now()).coerceAtLeast(kotlin.time.Duration.ZERO)) { wake.receive() }
            }
        }
    }

    private suspend fun handle(event: RealtimeEvent.Other) {
        when (event.type) {
            "chat.changes" -> {
                val page = runCatching { json.decodeFromString(SyncPageDto.serializer(), event.dataJson) }.getOrNull() ?: return
                // A new message from the peer ends their "typing…".
                page.items.mapNotNull { it.message }.filterNot { it.mine }.forEach { typing.onPeerTyping(it.conversationId, typing = false) }
                if (!sync.applyPushed(page)) pull()
            }
            "chat.typing" -> {
                val typingEvent = runCatching { json.decodeFromString(TypingEventDto.serializer(), event.dataJson) }.getOrNull() ?: return
                typing.onPeerTyping(typingEvent.conversationId, typingEvent.typing)
            }
        }
    }
}
