package com.laarasoft.moepay.chat.data.sync

import com.laarasoft.moepay.chat.data.remote.ChatRemoteDataSource
import com.laarasoft.moepay.chat.data.remote.SyncItemDto
import com.laarasoft.moepay.chat.data.remote.SyncPageDto
import com.laarasoft.moepay.chat.data.remote.toDomain
import com.laarasoft.moepay.chat.domain.ChatLocalDataSource
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.EmptyResult
import com.laarasoft.moepay.core.domain.util.Result
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val PAGE_SIZE = 200

/**
 * Keeps the device database in step with the server's per-user change feed.
 *
 * The feed is ordered and gap-free, so the device only stores one number (the cursor).
 * Each page is written in one transaction together with the new cursor: a crash between
 * pages resumes where it stopped, and nothing can be skipped.
 */
class ChatSyncEngine(
    private val remote: ChatRemoteDataSource,
    private val local: ChatLocalDataSource,
) {
    private val mutex = Mutex()

    /** Pulls every change since the cursor. Concurrent callers wait for the same pass. */
    suspend fun pull(): EmptyResult<DataError.Remote> = mutex.withLock {
        var cursor = local.syncCursor()
        do {
            val page = when (val result = remote.sync(after = cursor, limit = PAGE_SIZE)) {
                is Result.Failure -> return@withLock result
                is Result.Success -> result.data
            }
            val next = maxOf(cursor, page.nextAfter)
            apply(page.items, cursor = next)
            cursor = next
        } while (page.hasMore)
        Result.Success(Unit)
    }

    /**
     * Applies a page pushed over the WebSocket when it continues exactly from the cursor.
     * Returns false when there's a gap (missed events): the caller then pulls instead.
     */
    suspend fun applyPushed(page: SyncPageDto): Boolean = mutex.withLock {
        val cursor = local.syncCursor()
        val fresh = page.items.filter { it.seq > cursor }.sortedBy { it.seq }
        if (fresh.isEmpty()) return true
        if (fresh.first().seq != cursor + 1) return false
        apply(fresh, cursor = fresh.last().seq)
        true
    }

    private suspend fun apply(items: List<SyncItemDto>, cursor: Long) {
        local.applyChanges(
            conversations = items.mapNotNull { it.conversation?.toDomain() },
            messages = items.mapNotNull { it.message?.toDomain() },
            cursor = cursor,
        )
    }
}
