package com.laarasoft.moepay.core.domain.realtime

import com.laarasoft.moepay.core.domain.money.MoneyMovement
import com.laarasoft.moepay.core.domain.money.MoneyRequest
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/** Server pushes. They only notify: money moves through REST alone. */
sealed interface RealtimeEvent {
    data class WalletCredited(val movement: MoneyMovement) : RealtimeEvent
    data class WalletDebited(val movement: MoneyMovement) : RealtimeEvent
    data class MoneyRequestReceived(val request: MoneyRequest) : RealtimeEvent
    data class MoneyRequestUpdated(val request: MoneyRequest) : RealtimeEvent

    /**
     * Any other event type, with its `data` as raw JSON. Features decode their own events
     * (e.g. `chat.changes`) so core doesn't need to know their shapes.
     */
    data class Other(val type: String, val dataJson: String) : RealtimeEvent
}

enum class RealtimeConnection {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
}

interface RealtimeEvents {
    val events: SharedFlow<RealtimeEvent>

    val connection: StateFlow<RealtimeConnection>

    /** Connect (idempotent). Called while signed in and in the foreground. */
    fun start()
    fun stop()

    /**
     * Sends a client message `{type, data}` if connected; [dataJson] is a JSON object.
     * Returns false when there is no connection (the message is dropped, never queued).
     */
    fun send(type: String, dataJson: String): Boolean
}
