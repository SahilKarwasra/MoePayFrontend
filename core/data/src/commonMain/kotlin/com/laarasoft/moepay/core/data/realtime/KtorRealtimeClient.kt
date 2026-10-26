package com.laarasoft.moepay.core.data.realtime

import co.touchlab.kermit.Logger
import com.laarasoft.moepay.core.data.di.ApplicationScope
import com.laarasoft.moepay.core.data.money.MoneyMovementDto
import com.laarasoft.moepay.core.data.money.MoneyRequestDto
import com.laarasoft.moepay.core.data.money.toDomain
import com.laarasoft.moepay.core.data.networking.UrlConstants
import com.laarasoft.moepay.core.domain.auth.SessionStorage
import com.laarasoft.moepay.core.domain.realtime.RealtimeConnection
import com.laarasoft.moepay.core.domain.realtime.RealtimeEvent
import com.laarasoft.moepay.core.domain.realtime.RealtimeEvents
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Close code the backend uses when the session is signed out elsewhere. */
private const val CLOSE_SESSION_REVOKED: Short = 4001
private const val MAX_BACKOFF_MS = 30_000L

/**
 * Listens on `/api/v1/ws` (Bearer auth comes from the shared client's Auth plugin) and
 * reconnects with exponential backoff. It only notifies; money never moves over the socket.
 */
class KtorRealtimeClient(
    private val httpClient: HttpClient,
    private val json: Json,
    private val sessionStorage: SessionStorage,
    private val applicationScope: ApplicationScope,
) : RealtimeEvents {

    private val logger = Logger.withTag("MoePayRealtime")
    private val _events = MutableSharedFlow<RealtimeEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<RealtimeEvent> = _events.asSharedFlow()

    private val _connection = MutableStateFlow(RealtimeConnection.DISCONNECTED)
    override val connection: StateFlow<RealtimeConnection> = _connection.asStateFlow()

    private var connectionJob: Job? = null
    private var session: DefaultClientWebSocketSession? = null

    override fun start() {
        if (connectionJob?.isActive == true) return
        connectionJob = applicationScope.launch {
            try {
                connectLoop()
            } finally {
                session = null
                _connection.value = RealtimeConnection.DISCONNECTED
            }
        }
    }

    override fun stop() {
        connectionJob?.cancel()
        connectionJob = null
    }

    override fun send(type: String, dataJson: String): Boolean {
        val current = session ?: return false
        if (_connection.value != RealtimeConnection.CONNECTED) return false
        applicationScope.launch {
            runCatching { current.send(Frame.Text("""{"type":${json.encodeToString(type)},"data":$dataJson}""")) }
                .onFailure { logger.d { "send failed: ${it.message}" } }
        }
        return true
    }

    private suspend fun connectLoop() {
        var attempt = 0
        while (currentCoroutineContext().isActive) {
            var revoked = false
            _connection.value = RealtimeConnection.CONNECTING
            try {
                httpClient.webSocket(urlString = webSocketUrl()) {
                    attempt = 0
                    session = this
                    _connection.value = RealtimeConnection.CONNECTED
                    logger.d { "connected" }
                    for (frame in incoming) {
                        if (frame is Frame.Text) handle(frame.readText())
                    }
                    revoked = closeReason.await()?.code == CLOSE_SESSION_REVOKED
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.d { "socket failed: ${e.message}" }
            } finally {
                session = null
                _connection.value = RealtimeConnection.DISCONNECTED
            }
            if (revoked) {
                // Signed out on another device or the server revoked the session.
                sessionStorage.set(null)
                return
            }
            delay(minOf(MAX_BACKOFF_MS, 1_000L shl attempt.coerceAtMost(5)))
            attempt++
        }
    }

    private fun handle(text: String) {
        val envelope = runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull() ?: return
        val type = envelope["type"]?.jsonPrimitive?.content ?: return
        val data = envelope["data"] as? JsonObject ?: return
        val event = runCatching {
            when (type) {
                "connected" -> null
                "wallet.credited" -> RealtimeEvent.WalletCredited(json.decodeFromJsonElement(MoneyMovementDto.serializer(), data).toDomain())
                "wallet.debited" -> RealtimeEvent.WalletDebited(json.decodeFromJsonElement(MoneyMovementDto.serializer(), data).toDomain())
                "money_request.received" -> RealtimeEvent.MoneyRequestReceived(json.decodeFromJsonElement(MoneyRequestDto.serializer(), data).toDomain())
                "money_request.updated" -> RealtimeEvent.MoneyRequestUpdated(json.decodeFromJsonElement(MoneyRequestDto.serializer(), data).toDomain())
                else -> RealtimeEvent.Other(type, data.toString())
            }
        }.onFailure { logger.w { "bad $type event: ${it.message}" } }.getOrNull() ?: return
        _events.tryEmit(event)
    }

    private fun webSocketUrl(): String =
        UrlConstants.BASE_URL.replaceFirst("http", "ws") + UrlConstants.API_PATH + "/ws"
}
