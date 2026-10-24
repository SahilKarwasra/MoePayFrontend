package com.laarasoft.moepay.core.data.wallet

import com.laarasoft.moepay.core.data.networking.ApiFailure
import com.laarasoft.moepay.core.data.networking.mapApiError
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.wallet.WalletError
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class WalletErrorMapperTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun details(raw: String) = json.parseToJsonElement(raw).jsonObject

    @Test
    fun mapsBackendCodesByNameAndAlias() {
        assertEquals(DataError.Remote.INSUFFICIENT_BALANCE, mapApiError("INSUFFICIENT_BALANCE", 422))
        assertEquals(DataError.Remote.SESSION_EXPIRED, mapApiError("SESSION_REVOKED", 401))
        assertEquals(DataError.Remote.TOO_MANY_REQUESTS, mapApiError("RATE_LIMITED", 429))
        assertEquals(DataError.Remote.SERVER_ERROR, mapApiError(null, 502))
        assertEquals(DataError.Remote.UNKNOWN, mapApiError("SOMETHING_NEW", 418))
    }

    @Test
    fun keepsPinAttemptsRemaining() {
        val error = ApiFailure(DataError.Remote.PIN_INVALID, details("""{"attempts_remaining": 2}""")).toWalletError(json)
        assertEquals(WalletError.PinInvalid(attemptsRemaining = 2), error)
    }

    @Test
    fun keepsTheLockExpiry() {
        val error = ApiFailure(DataError.Remote.PIN_LOCKED, details("""{"locked_until": "2026-10-23T14:30:00Z", "retry_after_seconds": 1800}""")).toWalletError(json)
        assertEquals(WalletError.PinLocked(Instant.parse("2026-10-23T14:30:00Z")), error)
    }

    @Test
    fun keepsTheBankDeclineReasonAndFailedTransaction() {
        val raw = """
            {"reason": "Insufficient funds", "bank_code": "INSUFFICIENT_FUNDS", "transaction": {
              "id": "t1", "reference": "MP-1", "type": "add_money", "status": "failed", "direction": "in",
              "title": "Add money failed", "amount": {"amount_pence": 500, "currency": "GBP", "display": "£5.00"},
              "signed_display": "+£5.00", "counterparty": {"kind": "bank", "display_name": "MoeBank", "bank_name": "MoeBank", "account_last4": "0000"},
              "failure": {"code": "INSUFFICIENT_FUNDS", "reason": "Insufficient funds"}, "created_at": "2026-10-23T10:00:00Z"
            }}
        """.trimIndent()
        val error = ApiFailure(DataError.Remote.BANK_DECLINED, details(raw)).toWalletError(json)
        assertIs<WalletError.BankDeclined>(error)
        assertEquals("Insufficient funds", error.reason)
        assertEquals("t1", error.transaction?.id)
        assertEquals(true, error.transaction?.isFailed)
    }

    @Test
    fun otherErrorsStayRemote() {
        assertEquals(WalletError.Remote(DataError.Remote.LIMIT_EXCEEDED), ApiFailure(DataError.Remote.LIMIT_EXCEEDED).toWalletError(json))
    }
}
