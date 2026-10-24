package com.laarasoft.moepay.wallet.data

import com.laarasoft.moepay.core.data.money.MoneyDto
import com.laarasoft.moepay.core.data.money.PinStatusDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SetPinRequest(
    val pin: String,
    @SerialName("confirm_pin") val confirmPin: String,
)

@Serializable
internal data class ChangePinRequest(
    @SerialName("current_pin") val currentPin: String,
    @SerialName("new_pin") val newPin: String,
)

@Serializable
internal data class ResetPinRequest(
    val otp: String,
    @SerialName("new_pin") val newPin: String,
)

@Serializable
internal data class PinResponseDto(val pin: PinStatusDto)

@Serializable
internal data class LinkBankAccountRequest(
    @SerialName("holder_name") val holderName: String,
    @SerialName("sort_code") val sortCode: String,
    @SerialName("account_number") val accountNumber: String,
    @SerialName("make_primary") val makePrimary: Boolean,
)

@Serializable
internal data class SortCodeDto(
    @SerialName("sort_code") val sortCode: String,
    @SerialName("bank_name") val bankName: String,
)

@Serializable
internal data class BankTransferRequest(
    @SerialName("bank_account_id") val bankAccountId: String,
    @SerialName("amount_pence") val amountPence: Long,
    val pin: String,
)

@Serializable
internal data class PaymentQrDto(
    val payload: String,
    val username: String,
    @SerialName("display_name") val displayName: String,
    val amount: MoneyDto? = null,
    val note: String? = null,
)
