package com.laarasoft.moepay.chat.data.remote

import com.laarasoft.moepay.core.data.money.RecipientRefDto
import com.laarasoft.moepay.core.data.networking.ApiFailure
import com.laarasoft.moepay.core.data.networking.deleteDetailed
import com.laarasoft.moepay.core.data.networking.get
import com.laarasoft.moepay.core.data.networking.getDetailed
import com.laarasoft.moepay.core.data.networking.idempotencyKey
import com.laarasoft.moepay.core.data.networking.patchDetailed
import com.laarasoft.moepay.core.data.networking.post
import com.laarasoft.moepay.core.data.networking.postDetailed
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Result
import com.laarasoft.moepay.core.domain.wallet.IdempotencyKey
import io.ktor.client.HttpClient

/** The chat REST API. Write calls keep `error.details` (conflicts carry the current message). */
interface ChatRemoteDataSource {
    suspend fun sync(after: Long, limit: Int): Result<SyncPageDto, DataError.Remote>
    suspend fun open(with: RecipientRefDto): Result<ConversationDto, DataError.Remote>
    suspend fun messagesBefore(conversationId: String, beforeSeq: Long, limit: Int): Result<MessagePageDto, ApiFailure>
    suspend fun send(conversationId: String, request: SendMessageRequest): Result<MessageDto, ApiFailure>
    suspend fun edit(conversationId: String, messageId: String, request: EditMessageRequest): Result<MessageDto, ApiFailure>
    suspend fun delete(conversationId: String, messageId: String): Result<MessageDto, ApiFailure>
    suspend fun markRead(conversationId: String, upToSeq: Long): Result<ConversationDto, ApiFailure>
    suspend fun requestMoney(conversationId: String, request: ChatMoneyRequestRequest): Result<MessageResultDto, ApiFailure>
    suspend fun pay(conversationId: String, key: IdempotencyKey, request: ChatPaymentRequest): Result<MessageResultDto, ApiFailure>
}

class KtorChatRemoteDataSource(
    private val httpClient: HttpClient,
) : ChatRemoteDataSource {

    override suspend fun sync(after: Long, limit: Int): Result<SyncPageDto, DataError.Remote> =
        httpClient.get(route = "/chat/sync", queryParams = mapOf("after" to after, "limit" to limit))

    override suspend fun open(with: RecipientRefDto): Result<ConversationDto, DataError.Remote> =
        httpClient.post(route = "/conversations", body = OpenConversationRequest(with))

    override suspend fun messagesBefore(conversationId: String, beforeSeq: Long, limit: Int): Result<MessagePageDto, ApiFailure> =
        httpClient.getDetailed(
            route = "/conversations/$conversationId/messages",
            queryParams = mapOf("before_seq" to beforeSeq, "limit" to limit),
        )

    override suspend fun send(conversationId: String, request: SendMessageRequest): Result<MessageDto, ApiFailure> =
        httpClient.postDetailed(route = "/conversations/$conversationId/messages", body = request)

    override suspend fun edit(conversationId: String, messageId: String, request: EditMessageRequest): Result<MessageDto, ApiFailure> =
        httpClient.patchDetailed(route = "/conversations/$conversationId/messages/$messageId", body = request)

    override suspend fun delete(conversationId: String, messageId: String): Result<MessageDto, ApiFailure> =
        httpClient.deleteDetailed(route = "/conversations/$conversationId/messages/$messageId")

    override suspend fun markRead(conversationId: String, upToSeq: Long): Result<ConversationDto, ApiFailure> =
        httpClient.postDetailed(route = "/conversations/$conversationId/read", body = ReadRequest(upToSeq))

    override suspend fun requestMoney(conversationId: String, request: ChatMoneyRequestRequest): Result<MessageResultDto, ApiFailure> =
        httpClient.postDetailed(route = "/conversations/$conversationId/requests", body = request)

    override suspend fun pay(conversationId: String, key: IdempotencyKey, request: ChatPaymentRequest): Result<MessageResultDto, ApiFailure> =
        httpClient.postDetailed(
            route = "/conversations/$conversationId/payments",
            body = request,
            builder = { idempotencyKey(key) },
        )
}
