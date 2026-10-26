package com.laarasoft.moepay.chat.data.sync

import com.laarasoft.moepay.chat.data.remote.ChatMoneyRequestRequest
import com.laarasoft.moepay.chat.data.remote.ChatRemoteDataSource
import com.laarasoft.moepay.chat.data.remote.EditMessageRequest
import com.laarasoft.moepay.chat.data.remote.MessageDto
import com.laarasoft.moepay.chat.data.remote.SendMessageRequest
import com.laarasoft.moepay.chat.data.remote.toDomain
import com.laarasoft.moepay.chat.domain.ChatLocalDataSource
import com.laarasoft.moepay.chat.domain.Delivery
import com.laarasoft.moepay.chat.domain.OutboxOperation
import com.laarasoft.moepay.chat.domain.OutboxType
import com.laarasoft.moepay.core.data.networking.ApiFailure
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Result
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** Failures worth retrying: the request may succeed later without any change. */
private val transient = setOf(
    DataError.Remote.NO_INTERNET,
    DataError.Remote.REQUEST_TIMEOUT,
    DataError.Remote.SERVER_ERROR,
    DataError.Remote.SERVICE_UNAVAILABLE,
    DataError.Remote.SERIALIZATION,
    DataError.Remote.UNKNOWN,
    DataError.Remote.TOO_MANY_REQUESTS,
    DataError.Remote.UNAUTHORIZED,
    DataError.Remote.SESSION_EXPIRED,
    DataError.Remote.IDEMPOTENCY_KEY_IN_USE,
)

private val MAX_BACKOFF = 60.seconds

/**
 * Delivers the outbox. Operations of one conversation go strictly in the order they were
 * made: when one has to wait, the ones behind it wait too. Every write carries the message's
 * `client_message_id`, so resending after a lost response never creates a duplicate.
 */
class OutboxProcessor(
    private val remote: ChatRemoteDataSource,
    private val local: ChatLocalDataSource,
    private val json: Json,
    private val clock: Clock = Clock.System,
) {
    private val mutex = Mutex()

    private sealed interface Outcome {
        data object Done : Outcome
        data class Retry(val after: Duration?, val offline: Boolean = false) : Outcome
    }

    /**
     * Sends everything that is due. Returns when the earliest waiting operation should be
     * tried again, or null when the outbox is empty.
     */
    suspend fun drain(): Instant? = mutex.withLock {
        val waiting = mutableSetOf<String>()
        var nextAttempt: Instant? = null
        for (operation in local.pendingOperations()) {
            if (operation.conversationId in waiting) continue
            val now = clock.now()
            if (operation.nextAttemptAt > now) {
                waiting += operation.conversationId
                nextAttempt = minOf(nextAttempt ?: operation.nextAttemptAt, operation.nextAttemptAt)
                continue
            }
            when (val outcome = execute(operation)) {
                Outcome.Done -> local.removeOperation(operation.id)
                is Outcome.Retry -> {
                    val at = clock.now() + (outcome.after ?: backoff(operation.attempts))
                    local.updateOperation(operation.copy(attempts = operation.attempts + 1, nextAttemptAt = at))
                    nextAttempt = minOf(nextAttempt ?: at, at)
                    // Without a network every other call would fail the same way.
                    if (outcome.offline) return@withLock nextAttempt
                    waiting += operation.conversationId
                }
            }
        }
        nextAttempt
    }

    private suspend fun execute(operation: OutboxOperation): Outcome = when (operation.type) {
        OutboxType.SEND_TEXT -> sendText(operation)
        OutboxType.SEND_REQUEST -> sendRequest(operation)
        OutboxType.EDIT -> edit(operation)
        OutboxType.DELETE -> delete(operation)
        OutboxType.MARK_READ -> markRead(operation)
    }

    private suspend fun sendText(operation: OutboxOperation): Outcome {
        val localId = operation.localId ?: return Outcome.Done
        // Discarded by the user while waiting.
        local.getMessage(localId) ?: return Outcome.Done
        val result = remote.send(
            operation.conversationId,
            SendMessageRequest(clientMessageId = localId, body = operation.body.orEmpty(), replyToId = operation.replyToId),
        )
        return handleSend(localId, result.map { it })
    }

    private suspend fun sendRequest(operation: OutboxOperation): Outcome {
        val localId = operation.localId ?: return Outcome.Done
        local.getMessage(localId) ?: return Outcome.Done
        val result = remote.requestMoney(
            operation.conversationId,
            ChatMoneyRequestRequest(clientMessageId = localId, amountPence = operation.amountPence ?: 0, note = operation.body),
        )
        return handleSend(localId, result.map { it.message })
    }

    private suspend fun handleSend(localId: String, result: Result<MessageDto, ApiFailure>): Outcome = when (result) {
        is Result.Success -> {
            local.upsertServerMessages(listOf(result.data.toDomain()))
            Outcome.Done
        }
        is Result.Failure -> retryOr(result.error) {
            local.getMessage(localId)?.let { message ->
                local.updateMessage(message.copy(delivery = Delivery.FAILED, failure = result.error.error))
            }
        }
    }

    private suspend fun edit(operation: OutboxOperation): Outcome {
        val message = operation.localId?.let { local.getMessage(it) } ?: return Outcome.Done
        val serverId = message.serverId ?: return Outcome.Done
        val result = remote.edit(
            operation.conversationId,
            serverId,
            EditMessageRequest(body = operation.body.orEmpty(), baseVersion = operation.baseVersion),
        )
        return settleChange(operation, result)
    }

    private suspend fun delete(operation: OutboxOperation): Outcome {
        val message = operation.localId?.let { local.getMessage(it) } ?: return Outcome.Done
        val serverId = message.serverId ?: return Outcome.Done
        return settleChange(operation, remote.delete(operation.conversationId, serverId))
    }

    /**
     * Edits and deletes are shown optimistically. On success the server's copy replaces the
     * local one; on a permanent failure (window passed, changed on another device) the
     * server's current copy is restored so the screen never shows something that isn't true.
     */
    private suspend fun settleChange(operation: OutboxOperation, result: Result<MessageDto, ApiFailure>): Outcome = when (result) {
        is Result.Success -> {
            local.removeOperation(operation.id)
            local.upsertServerMessages(listOf(result.data.toDomain()))
            Outcome.Done
        }
        is Result.Failure -> retryOr(result.error) {
            local.removeOperation(operation.id)
            restoreServerCopy(operation, result.error)
        }
    }

    private suspend fun restoreServerCopy(operation: OutboxOperation, failure: ApiFailure) {
        val current = failure.details?.get("current")?.let { runCatching { json.decodeFromJsonElement(MessageDto.serializer(), it) }.getOrNull() }
        if (current != null) {
            local.upsertServerMessages(listOf(current.toDomain()))
            return
        }
        val seq = operation.localId?.let { local.getMessage(it) }?.seq ?: return
        val page = remote.messagesBefore(operation.conversationId, beforeSeq = seq + 1, limit = 1)
        if (page is Result.Success) local.upsertServerMessages(page.data.items.map { it.toDomain() })
    }

    private suspend fun markRead(operation: OutboxOperation): Outcome {
        val upTo = operation.upToSeq ?: return Outcome.Done
        return when (val result = remote.markRead(operation.conversationId, upTo)) {
            is Result.Success -> {
                local.upsertServerConversation(result.data.toDomain())
                Outcome.Done
            }
            // A read receipt that can't be delivered isn't worth bothering the user about.
            is Result.Failure -> retryOr(result.error) {}
        }
    }

    private inline fun retryOr(failure: ApiFailure, onPermanent: () -> Unit): Outcome {
        if (failure.error in transient) {
            val retryAfter = failure.details?.get("retry_after_seconds")?.jsonPrimitive?.intOrNull?.seconds
            return Outcome.Retry(after = retryAfter, offline = failure.error == DataError.Remote.NO_INTERNET)
        }
        onPermanent()
        return Outcome.Done
    }

    private fun backoff(attempts: Int): Duration =
        minOf(MAX_BACKOFF, (1L shl attempts.coerceAtMost(6)).seconds)
}

private inline fun <T, R> Result<T, ApiFailure>.map(transform: (T) -> R): Result<R, ApiFailure> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Failure -> this
}
