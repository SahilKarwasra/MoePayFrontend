package com.laarasoft.moepay.payments.data

import com.laarasoft.moepay.core.data.di.ApplicationScope
import com.laarasoft.moepay.core.data.money.MoneyMovementDto
import com.laarasoft.moepay.core.data.money.MoneyRequestDto
import com.laarasoft.moepay.core.data.money.MoneyRequestPageDto
import com.laarasoft.moepay.core.data.money.toDomain
import com.laarasoft.moepay.core.data.money.toDto
import com.laarasoft.moepay.core.data.money.toQuery
import com.laarasoft.moepay.core.data.money.toRecentRecipient
import com.laarasoft.moepay.core.data.networking.get
import com.laarasoft.moepay.core.data.networking.idempotencyKey
import com.laarasoft.moepay.core.data.networking.post
import com.laarasoft.moepay.core.data.networking.postDetailed
import com.laarasoft.moepay.core.data.networking.retryIdempotent
import com.laarasoft.moepay.core.data.wallet.asWalletResult
import com.laarasoft.moepay.core.domain.money.MoneyMovement
import com.laarasoft.moepay.core.domain.money.MoneyRequest
import com.laarasoft.moepay.core.domain.money.MoneyRequestDirection
import com.laarasoft.moepay.core.domain.money.MoneyRequestPage
import com.laarasoft.moepay.core.domain.money.MoneyRequestStatus
import com.laarasoft.moepay.core.domain.money.RecentRecipient
import com.laarasoft.moepay.core.domain.money.Recipient
import com.laarasoft.moepay.core.domain.money.RecipientRef
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Result
import com.laarasoft.moepay.core.domain.util.map
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.domain.wallet.IdempotencyKey
import com.laarasoft.moepay.core.domain.wallet.WalletError
import com.laarasoft.moepay.core.domain.wallet.WalletRepository
import com.laarasoft.moepay.core.domain.money.Money
import com.laarasoft.moepay.payments.domain.PaidRequest
import com.laarasoft.moepay.payments.domain.PaymentsService
import com.laarasoft.moepay.payments.domain.ResolvedQr
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class KtorPaymentsService(
    private val httpClient: HttpClient,
    private val json: Json,
    private val walletRepository: WalletRepository,
    private val applicationScope: ApplicationScope,
) : PaymentsService {

    override suspend fun resolveRecipient(ref: RecipientRef): Result<Recipient, DataError.Remote> =
        httpClient.get<RecipientEnvelopeDto>("/recipients/resolve", queryParams = mapOf(ref.toQuery()))
            .map { it.recipient.toDomain() }

    override suspend fun resolveQr(payload: String): Result<ResolvedQr, DataError.Remote> =
        httpClient.get<ResolvedQrDto>("/recipients/resolve", queryParams = mapOf("qr" to payload.trim()))
            .map { ResolvedQr(it.recipient.toDomain(), it.amount?.toDomain(), it.note) }

    override suspend fun recentRecipients(): Result<List<RecentRecipient>, DataError.Remote> =
        httpClient.get<RecipientsDto>("/recipients/recent")
            .map { dto -> dto.recipients.map { it.toRecentRecipient() } }

    override suspend fun transfer(
        to: RecipientRef,
        amountPence: Long,
        note: String?,
        pin: String,
        key: IdempotencyKey,
    ): Result<MoneyMovement, WalletError> = retryIdempotent {
        httpClient.postDetailed<TransferRequest, MoneyMovementDto>(
            route = "/transfers",
            body = TransferRequest(to = to.toDto(), amountPence = amountPence, note = note?.trim()?.ifEmpty { null }, pin = pin),
            builder = { idempotencyKey(key) },
        ).asWalletResult(json)
    }
        .map { it.toDomain() }
        .onSuccess { refreshWallet() }

    override suspend fun requestMoney(from: RecipientRef, amountPence: Long, note: String?): Result<MoneyRequest, DataError.Remote> =
        httpClient.post<CreateMoneyRequest, MoneyRequestDto>(
            route = "/money-requests",
            body = CreateMoneyRequest(from = from.toDto(), amountPence = amountPence, note = note?.trim()?.ifEmpty { null }),
        )
            .map { it.toDomain() }
            .onSuccess { refreshWallet() }

    override suspend fun moneyRequests(
        direction: MoneyRequestDirection,
        status: MoneyRequestStatus?,
        cursor: String?,
    ): Result<MoneyRequestPage, DataError.Remote> {
        val params = buildMap<String, Any> {
            put("direction", if (direction == MoneyRequestDirection.INCOMING) "incoming" else "outgoing")
            status?.let { put("status", it.name.lowercase()) }
            cursor?.let { put("cursor", it) }
            put("limit", 20)
        }
        return httpClient.get<MoneyRequestPageDto>("/money-requests", queryParams = params).map { it.toDomain() }
    }

    override suspend fun moneyRequest(id: String): Result<MoneyRequest, DataError.Remote> =
        httpClient.get<MoneyRequestDto>("/money-requests/$id").map { it.toDomain() }

    override suspend fun payRequest(id: String, pin: String, key: IdempotencyKey): Result<PaidRequest, WalletError> = retryIdempotent {
        httpClient.postDetailed<PayRequestBody, PaidRequestDto>(
            route = "/money-requests/$id/pay",
            body = PayRequestBody(pin),
            builder = { idempotencyKey(key) },
        ).asWalletResult(json)
    }
        .map { dto ->
            PaidRequest(
                movement = MoneyMovement(dto.transaction.toDomain(), Money(dto.balance.amountPence, dto.balance.display)),
                request = dto.request.toDomain(),
            )
        }
        .onSuccess { refreshWallet() }

    override suspend fun declineRequest(id: String): Result<MoneyRequest, DataError.Remote> =
        httpClient.post<JsonObject, MoneyRequestDto>("/money-requests/$id/decline", JsonObject(emptyMap()))
            .map { it.toDomain() }
            .onSuccess { refreshWallet() }

    override suspend fun cancelRequest(id: String): Result<MoneyRequest, DataError.Remote> =
        httpClient.post<JsonObject, MoneyRequestDto>("/money-requests/$id/cancel", JsonObject(emptyMap()))
            .map { it.toDomain() }
            .onSuccess { refreshWallet() }

    private fun refreshWallet() {
        applicationScope.launch { walletRepository.refresh() }
    }
}
