package com.laarasoft.moepay.core.domain.realtime

import com.laarasoft.moepay.core.domain.money.MoneyMovement
import com.laarasoft.moepay.core.domain.money.MoneyRequest
import kotlinx.coroutines.flow.SharedFlow

/** Server pushes. They only notify: money moves through REST alone. */
sealed interface RealtimeEvent {
    data class WalletCredited(val movement: MoneyMovement) : RealtimeEvent
    data class WalletDebited(val movement: MoneyMovement) : RealtimeEvent
    data class MoneyRequestReceived(val request: MoneyRequest) : RealtimeEvent
    data class MoneyRequestUpdated(val request: MoneyRequest) : RealtimeEvent
}

interface RealtimeEvents {
    val events: SharedFlow<RealtimeEvent>

    /** Connect (idempotent). Called while signed in and in the foreground. */
    fun start()
    fun stop()
}
