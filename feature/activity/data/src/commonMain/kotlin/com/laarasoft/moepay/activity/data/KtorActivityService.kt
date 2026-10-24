package com.laarasoft.moepay.activity.data

import com.laarasoft.moepay.activity.domain.ActivityService
import com.laarasoft.moepay.activity.domain.HistoryFilter
import com.laarasoft.moepay.core.data.money.TransactionDto
import com.laarasoft.moepay.core.data.money.TransactionPageDto
import com.laarasoft.moepay.core.data.money.toDomain
import com.laarasoft.moepay.core.data.networking.get
import com.laarasoft.moepay.core.domain.money.Transaction
import com.laarasoft.moepay.core.domain.money.TransactionPage
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.Result
import com.laarasoft.moepay.core.domain.util.map
import io.ktor.client.HttpClient

private const val PAGE_SIZE = 30

class KtorActivityService(
    private val httpClient: HttpClient,
) : ActivityService {

    override suspend fun transactions(filter: HistoryFilter, cursor: String?): Result<TransactionPage, DataError.Remote> {
        val params = buildMap<String, Any> {
            put("limit", PAGE_SIZE)
            cursor?.let { put("cursor", it) }
            when (filter) {
                HistoryFilter.ALL -> Unit
                HistoryFilter.MONEY_IN -> {
                    put("direction", "in")
                    put("status", "succeeded")
                }
                HistoryFilter.MONEY_OUT -> {
                    put("direction", "out")
                    put("status", "succeeded")
                }
                HistoryFilter.FAILED -> put("status", "failed")
            }
        }
        return httpClient.get<TransactionPageDto>("/transactions", queryParams = params).map { it.toDomain() }
    }

    override suspend fun transaction(id: String): Result<Transaction, DataError.Remote> =
        httpClient.get<TransactionDto>("/transactions/$id").map { it.toDomain() }
}
