package com.laarasoft.moepay.wallet.data

import com.laarasoft.moepay.core.data.di.ApplicationScope
import com.laarasoft.moepay.core.data.dto.SendOtpResponseDto
import com.laarasoft.moepay.core.data.money.BankAccountDto
import com.laarasoft.moepay.core.data.money.MoneyMovementDto
import com.laarasoft.moepay.core.data.money.toDomain
import com.laarasoft.moepay.core.data.networking.delete
import com.laarasoft.moepay.core.data.networking.get
import com.laarasoft.moepay.core.data.networking.idempotencyKey
import com.laarasoft.moepay.core.data.networking.post
import com.laarasoft.moepay.core.data.networking.postDetailed
import com.laarasoft.moepay.core.data.networking.put
import com.laarasoft.moepay.core.data.networking.putDetailed
import com.laarasoft.moepay.core.data.networking.retryIdempotent
import com.laarasoft.moepay.core.data.wallet.asWalletResult
import com.laarasoft.moepay.core.domain.auth.OtpChallenge
import com.laarasoft.moepay.core.domain.money.MoneyMovement
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.EmptyResult
import com.laarasoft.moepay.core.domain.util.Result
import com.laarasoft.moepay.core.domain.util.asEmptyResult
import com.laarasoft.moepay.core.domain.util.map
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.domain.wallet.BankAccount
import com.laarasoft.moepay.core.domain.wallet.IdempotencyKey
import com.laarasoft.moepay.core.domain.wallet.PinStatus
import com.laarasoft.moepay.core.domain.wallet.WalletError
import com.laarasoft.moepay.core.domain.wallet.WalletRepository
import com.laarasoft.moepay.wallet.domain.BankDetailsValidator
import com.laarasoft.moepay.wallet.domain.BankTransferDirection
import com.laarasoft.moepay.wallet.domain.LinkBankAccountInput
import com.laarasoft.moepay.wallet.domain.PaymentQr
import com.laarasoft.moepay.wallet.domain.WalletService
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class KtorWalletService(
    private val httpClient: HttpClient,
    private val json: Json,
    private val walletRepository: WalletRepository,
    private val applicationScope: ApplicationScope,
) : WalletService {

    override suspend fun setPin(pin: String, confirmPin: String): Result<PinStatus, WalletError> =
        httpClient.postDetailed<SetPinRequest, PinResponseDto>("/wallet/pin", SetPinRequest(pin, confirmPin))
            .asWalletResult(json)
            .map { it.pin.toDomain() }
            .onSuccess { refreshWallet() }

    override suspend fun changePin(currentPin: String, newPin: String): Result<PinStatus, WalletError> =
        httpClient.putDetailed<ChangePinRequest, PinResponseDto>("/wallet/pin", ChangePinRequest(currentPin, newPin))
            .asWalletResult(json)
            .map { it.pin.toDomain() }
            .onSuccess { refreshWallet() }

    override suspend fun sendPinResetOtp(): Result<OtpChallenge, DataError.Remote> =
        httpClient.post<JsonObject, SendOtpResponseDto>("/wallet/pin/reset/otp", JsonObject(emptyMap()))
            .map { OtpChallenge(it.phoneNumber, it.expiresInSeconds, it.resendAfterSeconds) }

    override suspend fun resetPin(otp: String, newPin: String): Result<PinStatus, WalletError> =
        httpClient.postDetailed<ResetPinRequest, PinResponseDto>("/wallet/pin/reset", ResetPinRequest(otp, newPin))
            .asWalletResult(json)
            .map { it.pin.toDomain() }
            .onSuccess { refreshWallet() }

    override suspend fun linkBankAccount(input: LinkBankAccountInput): Result<BankAccount, DataError.Remote> =
        httpClient.post<LinkBankAccountRequest, BankAccountDto>(
            route = "/bank-accounts",
            body = LinkBankAccountRequest(
                holderName = BankDetailsValidator.cleanHolderName(input.holderName),
                sortCode = BankDetailsValidator.digits(input.sortCode),
                accountNumber = BankDetailsValidator.digits(input.accountNumber),
                makePrimary = input.makePrimary,
            ),
        )
            .map { it.toDomain() }
            .onSuccess { refreshWallet() }

    override suspend fun removeBankAccount(id: String): EmptyResult<DataError.Remote> =
        httpClient.delete<JsonObject>("/bank-accounts/$id")
            .onSuccess { refreshWallet() }
            .asEmptyResult()

    override suspend fun makePrimary(id: String): Result<BankAccount, DataError.Remote> =
        httpClient.put<JsonObject, BankAccountDto>("/bank-accounts/$id/primary", body = JsonObject(emptyMap()))
            .map { it.toDomain() }
            .onSuccess { refreshWallet() }

    override suspend fun lookupSortCode(sortCode: String): Result<String, DataError.Remote> =
        httpClient.get<SortCodeDto>("/banks/sort-code/${BankDetailsValidator.digits(sortCode)}")
            .map { it.bankName }

    override suspend fun bankTransfer(
        direction: BankTransferDirection,
        bankAccountId: String,
        amountPence: Long,
        pin: String,
        key: IdempotencyKey,
    ): Result<MoneyMovement, WalletError> {
        val route = when (direction) {
            BankTransferDirection.ADD_MONEY -> "/wallet/add-money"
            BankTransferDirection.WITHDRAW -> "/wallet/withdraw"
        }
        return retryIdempotent {
            httpClient.postDetailed<BankTransferRequest, MoneyMovementDto>(
                route = route,
                body = BankTransferRequest(bankAccountId, amountPence, pin),
                builder = { idempotencyKey(key) },
            ).asWalletResult(json)
        }
            .map { it.toDomain() }
            .onSuccess { refreshWallet() }
    }

    override suspend fun paymentQr(amountPence: Long?, note: String?): Result<PaymentQr, DataError.Remote> {
        val params = buildMap<String, Any> {
            if (amountPence != null && amountPence > 0) put("amount_pence", amountPence)
            if (!note.isNullOrBlank()) put("note", note.trim())
        }
        return httpClient.get<PaymentQrDto>("/wallet/qr", queryParams = params)
            .map { PaymentQr(it.payload, it.username, it.displayName, it.amount?.toDomain(), it.note) }
    }

    private fun refreshWallet() {
        applicationScope.launch { walletRepository.refresh() }
    }
}
