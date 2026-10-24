package com.laarasoft.moepay.payments.domain

import com.laarasoft.moepay.core.domain.money.Money
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
import com.laarasoft.moepay.core.domain.wallet.IdempotencyKey
import com.laarasoft.moepay.core.domain.wallet.WalletError

/** A scanned `moepay://pay` code, confirmed by the server. */
data class ResolvedQr(
    val recipient: Recipient,
    val amount: Money?,
    val note: String?,
)

data class PaidRequest(
    val movement: MoneyMovement,
    val request: MoneyRequest,
)

interface PaymentsService {
    suspend fun resolveRecipient(ref: RecipientRef): Result<Recipient, DataError.Remote>
    suspend fun resolveQr(payload: String): Result<ResolvedQr, DataError.Remote>
    suspend fun recentRecipients(): Result<List<RecentRecipient>, DataError.Remote>

    suspend fun transfer(
        to: RecipientRef,
        amountPence: Long,
        note: String?,
        pin: String,
        key: IdempotencyKey,
    ): Result<MoneyMovement, WalletError>

    suspend fun requestMoney(from: RecipientRef, amountPence: Long, note: String?): Result<MoneyRequest, DataError.Remote>

    suspend fun moneyRequests(
        direction: MoneyRequestDirection,
        status: MoneyRequestStatus?,
        cursor: String?,
    ): Result<MoneyRequestPage, DataError.Remote>

    suspend fun moneyRequest(id: String): Result<MoneyRequest, DataError.Remote>
    suspend fun payRequest(id: String, pin: String, key: IdempotencyKey): Result<PaidRequest, WalletError>
    suspend fun declineRequest(id: String): Result<MoneyRequest, DataError.Remote>
    suspend fun cancelRequest(id: String): Result<MoneyRequest, DataError.Remote>
}
