package com.laarasoft.moepay.payments.data

import com.laarasoft.moepay.core.data.money.MoneyDto
import com.laarasoft.moepay.core.data.money.MoneyRequestDto
import com.laarasoft.moepay.core.data.money.RecipientDto
import com.laarasoft.moepay.core.data.money.RecipientRefDto
import com.laarasoft.moepay.core.data.money.TransactionDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RecipientEnvelopeDto(val recipient: RecipientDto)

@Serializable
internal data class RecipientsDto(val recipients: List<RecipientDto> = emptyList())

@Serializable
internal data class ResolvedQrDto(
    val recipient: RecipientDto,
    val amount: MoneyDto? = null,
    val note: String? = null,
)

@Serializable
internal data class TransferRequest(
    val to: RecipientRefDto,
    @SerialName("amount_pence") val amountPence: Long,
    val note: String? = null,
    val pin: String,
)

@Serializable
internal data class CreateMoneyRequest(
    val from: RecipientRefDto,
    @SerialName("amount_pence") val amountPence: Long,
    val note: String? = null,
)

@Serializable
internal data class PayRequestBody(val pin: String)

/** Go embeds payment.Result into PayResult, so transaction and balance arrive at the top level. */
@Serializable
internal data class PaidRequestDto(
    val transaction: TransactionDto,
    val balance: MoneyDto,
    val request: MoneyRequestDto,
)
