package com.laarasoft.moepay.wallet.domain

import com.laarasoft.moepay.core.domain.auth.OtpChallenge
import com.laarasoft.moepay.core.domain.money.Money
import com.laarasoft.moepay.core.domain.money.MoneyMovement
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.EmptyResult
import com.laarasoft.moepay.core.domain.util.Result
import com.laarasoft.moepay.core.domain.wallet.BankAccount
import com.laarasoft.moepay.core.domain.wallet.IdempotencyKey
import com.laarasoft.moepay.core.domain.wallet.PinStatus
import com.laarasoft.moepay.core.domain.wallet.WalletError

data class PaymentQr(
    /** `moepay://pay?u=…&a=…&n=…` */
    val payload: String,
    val username: String,
    val displayName: String,
    val amount: Money?,
    val note: String?,
)

data class LinkBankAccountInput(
    val holderName: String,
    val sortCode: String,
    val accountNumber: String,
    val makePrimary: Boolean,
)

@kotlinx.serialization.Serializable
enum class BankTransferDirection {
    /** Pull from the bank into the wallet. */
    ADD_MONEY,

    /** Push from the wallet to the bank. */
    WITHDRAW,
}

/** Wallet management. Successful changes refresh the shared WalletRepository. */
interface WalletService {
    suspend fun setPin(pin: String, confirmPin: String): Result<PinStatus, WalletError>
    suspend fun changePin(currentPin: String, newPin: String): Result<PinStatus, WalletError>
    suspend fun sendPinResetOtp(): Result<OtpChallenge, DataError.Remote>
    suspend fun resetPin(otp: String, newPin: String): Result<PinStatus, WalletError>

    suspend fun linkBankAccount(input: LinkBankAccountInput): Result<BankAccount, DataError.Remote>
    suspend fun removeBankAccount(id: String): EmptyResult<DataError.Remote>
    suspend fun makePrimary(id: String): Result<BankAccount, DataError.Remote>

    /** Bank name for a 6-digit sort code. */
    suspend fun lookupSortCode(sortCode: String): Result<String, DataError.Remote>

    suspend fun bankTransfer(
        direction: BankTransferDirection,
        bankAccountId: String,
        amountPence: Long,
        pin: String,
        key: IdempotencyKey,
    ): Result<MoneyMovement, WalletError>

    suspend fun paymentQr(amountPence: Long?, note: String?): Result<PaymentQr, DataError.Remote>
}
