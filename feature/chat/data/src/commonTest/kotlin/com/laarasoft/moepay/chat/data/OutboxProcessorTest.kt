package com.laarasoft.moepay.chat.data

import com.laarasoft.moepay.chat.data.remote.toDomain
import com.laarasoft.moepay.chat.data.sync.OutboxProcessor
import com.laarasoft.moepay.chat.domain.ChatMessage
import com.laarasoft.moepay.chat.domain.Delivery
import com.laarasoft.moepay.chat.domain.MessageContent
import com.laarasoft.moepay.chat.domain.OutboxOperation
import com.laarasoft.moepay.chat.domain.OutboxType
import com.laarasoft.moepay.core.data.networking.ApiFailure
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class OutboxProcessorTest {
    private val local = FakeLocal()
    private val remote = FakeRemote()
    private val clock = object : Clock {
        override fun now(): Instant = epoch
    }
    private val outbox = OutboxProcessor(remote, local, Json { ignoreUnknownKeys = true }, clock)

    private suspend fun queueText(localId: String, body: String = "hi", conversationId: String = "c1") {
        local.insertPending(
            ChatMessage(
                localId = localId, serverId = null, conversationId = conversationId, seq = null, senderId = "me", isMine = true,
                content = MessageContent.Text(body), replyToId = null, version = 0, isEdited = false, createdAt = epoch,
                delivery = Delivery.PENDING,
            ),
            OutboxOperation(type = OutboxType.SEND_TEXT, conversationId = conversationId, localId = localId, body = body, nextAttemptAt = epoch, createdAt = epoch),
        )
    }

    @Test
    fun aSentMessageTurnsFromPendingIntoTheServersCopyInPlace() = runTest {
        queueText("client-1")

        assertNull(outbox.drain())

        val message = local.messages.getValue("client-1")
        assertEquals("srv-client-1", message.serverId)
        assertEquals(Delivery.SENT, message.delivery)
        assertEquals(10L, message.seq)
        assertTrue(local.operations.isEmpty())
        // The client id doubles as the idempotency key, so resending can't duplicate.
        assertEquals("client-1", remote.sent.single().clientMessageId)
    }

    @Test
    fun withoutNetworkTheMessageStaysPendingAndIsRetriedLater() = runTest {
        queueText("client-1")
        queueText("client-2")
        remote.sendResult = { Result.Failure(ApiFailure(DataError.Remote.NO_INTERNET)) }

        val retryAt = outbox.drain()

        assertNotNull(retryAt)
        assertTrue(retryAt > epoch)
        assertEquals(Delivery.PENDING, local.messages.getValue("client-1").delivery)
        assertEquals(2, local.operations.size)
        // Offline: it stops at the first failure instead of trying everything.
        assertEquals(1, remote.sent.size)
        assertEquals(1, local.operations.first().attempts)
    }

    @Test
    fun aRefusedMessageIsMarkedFailedWithTheReason() = runTest {
        queueText("client-1", body = "x")
        remote.sendResult = { Result.Failure(ApiFailure(DataError.Remote.MESSAGE_INVALID)) }

        outbox.drain()

        val message = local.messages.getValue("client-1")
        assertEquals(Delivery.FAILED, message.delivery)
        assertEquals(DataError.Remote.MESSAGE_INVALID, message.failure)
        assertTrue(local.operations.isEmpty())
    }

    @Test
    fun laterMessagesInAWaitingConversationKeepTheirOrder() = runTest {
        queueText("first")
        queueText("second")
        queueText("other", conversationId = "c2")
        remote.sendResult = { request ->
            if (request.clientMessageId == "first") {
                Result.Failure(ApiFailure(DataError.Remote.SERVER_ERROR))
            } else {
                Result.Success(messageDto(id = "srv-${request.clientMessageId}", seq = 1, clientId = request.clientMessageId))
            }
        }

        outbox.drain()

        assertEquals(listOf("first", "other"), remote.sent.map { it.clientMessageId })
        assertEquals(Delivery.PENDING, local.messages.getValue("second").delivery)
    }

    @Test
    fun aRateLimitWaitsAsLongAsTheServerAsks() = runTest {
        queueText("client-1")
        remote.sendResult = {
            Result.Failure(ApiFailure(DataError.Remote.TOO_MANY_REQUESTS, Json.parseToJsonElement("""{"retry_after_seconds":30}""").let { it as kotlinx.serialization.json.JsonObject }))
        }

        val retryAt = outbox.drain()

        assertEquals(epoch.plus(kotlin.time.Duration.parse("30s")), retryAt)
    }

    @Test
    fun anEditThatIsNoLongerAllowedRestoresTheServersCopy() = runTest {
        local.upsertServerMessages(listOf(messageDto(id = "m1", seq = 1, clientId = "client-1", body = "original").toDomain()))
        local.updateMessage(local.messages.getValue("client-1").copy(content = MessageContent.Text("changed"), isEdited = true))
        local.enqueue(OutboxOperation(type = OutboxType.EDIT, conversationId = "c1", localId = "client-1", body = "changed", baseVersion = 1, nextAttemptAt = epoch, createdAt = epoch))
        val current = Json.encodeToJsonElement(com.laarasoft.moepay.chat.data.remote.MessageDto.serializer(), messageDto(id = "m1", seq = 1, clientId = "client-1", body = "from other device", version = 2))
        remote.editResult = { _, _ ->
            Result.Failure(ApiFailure(DataError.Remote.MESSAGE_VERSION_CONFLICT, kotlinx.serialization.json.JsonObject(mapOf("current" to current))))
        }

        outbox.drain()

        assertEquals(MessageContent.Text("from other device"), local.messages.getValue("client-1").content)
        assertTrue(local.operations.isEmpty())
    }

    @Test
    fun readReceiptsAreCoalescedToTheHighestSeq() = runTest {
        local.enqueueMarkRead(OutboxOperation(type = OutboxType.MARK_READ, conversationId = "c1", upToSeq = 3, nextAttemptAt = epoch, createdAt = epoch))
        local.enqueueMarkRead(OutboxOperation(type = OutboxType.MARK_READ, conversationId = "c1", upToSeq = 7, nextAttemptAt = epoch, createdAt = epoch))

        assertEquals(1, local.operations.size)
        assertEquals(7L, local.operations.single().upToSeq)
        outbox.drain()
        assertTrue(local.operations.isEmpty())
    }
}
