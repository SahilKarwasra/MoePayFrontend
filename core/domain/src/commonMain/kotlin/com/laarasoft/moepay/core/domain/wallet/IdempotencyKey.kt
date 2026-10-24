package com.laarasoft.moepay.core.domain.wallet

import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Identifies one confirmed money-moving attempt. Retrying with the same key can never move
 * money twice; a new key is minted only when the user changes what they're sending or after
 * a definitive failure.
 */
@JvmInline
value class IdempotencyKey(val value: String) {
    companion object {
        @OptIn(ExperimentalUuidApi::class)
        fun new(): IdempotencyKey = IdempotencyKey(Uuid.random().toString())
    }
}

/**
 * Hands out the same [IdempotencyKey] while the user retries the same payment, and a fresh one
 * as soon as what they're paying changes (a different [fingerprint]) or after [invalidate].
 */
class IdempotencyKeyHolder {
    private var key: IdempotencyKey? = null
    private var fingerprint: Any? = null

    fun keyFor(fingerprint: Any): IdempotencyKey {
        val current = key
        if (current != null && this.fingerprint == fingerprint) return current
        return IdempotencyKey.new().also {
            key = it
            this.fingerprint = fingerprint
        }
    }

    /** Call after success or a definitive failure so the next attempt is a new payment. */
    fun invalidate() {
        key = null
        fingerprint = null
    }
}
