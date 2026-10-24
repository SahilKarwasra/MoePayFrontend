package com.laarasoft.moepay.activity.domain

import com.laarasoft.moepay.core.domain.money.Transaction
import com.laarasoft.moepay.core.domain.money.TransactionPage
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Result

/** History filters offered in the app; each maps onto `GET /transactions` query parameters. */
enum class HistoryFilter {
    ALL,
    MONEY_IN,
    MONEY_OUT,
    FAILED,
}

interface ActivityService {
    suspend fun transactions(filter: HistoryFilter, cursor: String?): Result<TransactionPage, DataError.Remote>
    suspend fun transaction(id: String): Result<Transaction, DataError.Remote>
}
