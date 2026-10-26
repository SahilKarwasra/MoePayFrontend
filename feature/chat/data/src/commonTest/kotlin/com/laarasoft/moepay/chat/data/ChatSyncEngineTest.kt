package com.laarasoft.moepay.chat.data

import com.laarasoft.moepay.chat.data.remote.SyncItemDto
import com.laarasoft.moepay.chat.data.remote.SyncPageDto
import com.laarasoft.moepay.chat.data.sync.ChatSyncEngine
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ChatSyncEngineTest {
    private val local = FakeLocal()
    private val remote = FakeRemote()
    private val engine = ChatSyncEngine(remote, local)

    private fun page(vararg seqs: Long, hasMore: Boolean = false) = SyncPageDto(
        items = seqs.map { SyncItemDto(seq = it, type = "message", message = messageDto(id = "m$it", seq = it, mine = false)) },
        nextAfter = seqs.lastOrNull() ?: 0,
        hasMore = hasMore,
    )

    @Test
    fun pullFollowsPagesAndStoresTheCursorWithEachPage() = runTest {
        remote.syncPages += Result.Success(page(1, 2, hasMore = true))
        remote.syncPages += Result.Success(page(3))

        assertIs<Result.Success<Unit>>(engine.pull())

        assertEquals(listOf(0L, 2L), remote.syncCalls)
        assertEquals(3, local.cursor)
        assertEquals(2, local.appliedPages)
        assertEquals(setOf("m1", "m2", "m3"), local.messages.keys)
    }

    @Test
    fun aFailedPageKeepsEverythingAppliedBeforeIt() = runTest {
        remote.syncPages += Result.Success(page(1, 2, hasMore = true))
        remote.syncPages += Result.Failure(DataError.Remote.NO_INTERNET)

        assertIs<Result.Failure<DataError.Remote>>(engine.pull())

        assertEquals(2, local.cursor)
        // Resuming asks for changes after what was already stored.
        remote.syncPages += Result.Success(page(3))
        engine.pull()
        assertEquals(2L, remote.syncCalls.last())
        assertEquals(3, local.cursor)
    }

    @Test
    fun pushedChangesThatContinueTheCursorApplyDirectly() = runTest {
        local.cursor = 4
        assertTrue(engine.applyPushed(page(5, 6)))
        assertEquals(6, local.cursor)
        assertTrue(remote.syncCalls.isEmpty())
    }

    @Test
    fun aGapInPushedChangesAsksForAPull() = runTest {
        local.cursor = 4
        assertFalse(engine.applyPushed(page(7)))
        assertEquals(4, local.cursor)
    }

    @Test
    fun alreadySeenPushedChangesAreIgnored() = runTest {
        local.cursor = 6
        assertTrue(engine.applyPushed(page(5, 6)))
        assertEquals(0, local.appliedPages)
    }
}
