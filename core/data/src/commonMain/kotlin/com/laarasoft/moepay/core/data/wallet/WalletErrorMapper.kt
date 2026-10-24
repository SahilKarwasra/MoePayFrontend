package com.laarasoft.moepay.core.data.wallet

import com.laarasoft.moepay.core.data.money.TransactionDto
import com.laarasoft.moepay.core.data.money.toDomain
import com.laarasoft.moepay.core.data.money.toInstantOrNull
import com.laarasoft.moepay.core.data.networking.ApiFailure
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Result
import com.laarasoft.moepay.core.domain.wallet.WalletError
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/** Keeps the details the UI reacts to: PIN attempts, lock expiry, bank decline reason. */
fun ApiFailure.toWalletError(json: Json): WalletError {
    val details = details
    return when (error) {
        DataError.Remote.PIN_INVALID -> WalletError.PinInvalid(
            attemptsRemaining = details?.get("attempts_remaining")?.jsonPrimitive?.intOrNull,
        )
        DataError.Remote.PIN_LOCKED -> WalletError.PinLocked(
            lockedUntil = details?.get("locked_until")?.jsonPrimitive?.content.toInstantOrNull(),
        )
        DataError.Remote.BANK_DECLINED -> WalletError.BankDeclined(
            reason = details?.get("reason")?.jsonPrimitive?.content,
            transaction = details?.get("transaction")?.let { element ->
                runCatching { json.decodeFromJsonElement(TransactionDto.serializer(), element).toDomain() }.getOrNull()
            },
        )
        else -> WalletError.Remote(error)
    }
}

fun <T> Result<T, ApiFailure>.asWalletResult(json: Json): Result<T, WalletError> = when (this) {
    is Result.Success -> this
    is Result.Failure -> Result.Failure(error.toWalletError(json))
}

fun <T> Result<T, ApiFailure>.asRemoteResult(): Result<T, DataError.Remote> = when (this) {
    is Result.Success -> this
    is Result.Failure -> Result.Failure(error.error)
}
