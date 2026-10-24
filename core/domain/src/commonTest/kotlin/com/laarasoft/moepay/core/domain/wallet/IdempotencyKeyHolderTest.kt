package com.laarasoft.moepay.core.domain.wallet

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class IdempotencyKeyHolderTest {
    @Test
    fun reusesTheKeyWhileThePaymentIsUnchanged() {
        val holder = IdempotencyKeyHolder()
        val first = holder.keyFor(Triple("alice", 1_000L, "lunch"))
        assertEquals(first, holder.keyFor(Triple("alice", 1_000L, "lunch")))
    }

    @Test
    fun mintsANewKeyWhenThePaymentChangesOrAfterInvalidate() {
        val holder = IdempotencyKeyHolder()
        val first = holder.keyFor(Triple("alice", 1_000L, "lunch"))
        val changed = holder.keyFor(Triple("alice", 1_500L, "lunch"))
        assertNotEquals(first, changed)

        holder.invalidate()
        assertNotEquals(changed, holder.keyFor(Triple("alice", 1_500L, "lunch")))
    }
}
