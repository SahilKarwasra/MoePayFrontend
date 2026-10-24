package com.laarasoft.moepay.core.data.wallet

import com.laarasoft.moepay.core.data.di.ApplicationScope
import com.laarasoft.moepay.core.data.money.WalletScreenDto
import com.laarasoft.moepay.core.data.money.toDomain
import com.laarasoft.moepay.core.data.networking.get
import com.laarasoft.moepay.core.domain.auth.SessionStorage
import com.laarasoft.moepay.core.domain.realtime.RealtimeEvents
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.EmptyResult
import com.laarasoft.moepay.core.domain.util.Result
import com.laarasoft.moepay.core.domain.util.asEmptyResult
import com.laarasoft.moepay.core.domain.util.map
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.core.domain.wallet.WalletOverview
import com.laarasoft.moepay.core.domain.wallet.WalletRepository
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class KtorWalletRepository(
    private val httpClient: HttpClient,
    realtimeEvents: RealtimeEvents,
    sessionStorage: SessionStorage,
    applicationScope: ApplicationScope,
) : WalletRepository {

    private val _overview = MutableStateFlow<WalletOverview?>(null)
    override val overview: StateFlow<WalletOverview?> = _overview.asStateFlow()

    private val refreshMutex = Mutex()

    init {
        // Money arrived or left from elsewhere (another device, a payer): reload once.
        realtimeEvents.events
            .onEach { refresh() }
            .launchIn(applicationScope)

        // Never show one user's wallet to the next person who signs in.
        sessionStorage.observeAuthInfo()
            .onEach { if (it == null) clear() }
            .launchIn(applicationScope)
    }

    override suspend fun refresh(): EmptyResult<DataError.Remote> {
        // Concurrent callers share one request instead of racing.
        if (refreshMutex.isLocked) {
            refreshMutex.withLock { }
            return Result.Success(Unit)
        }
        return refreshMutex.withLock {
            httpClient.get<WalletScreenDto>(route = "/wallet")
                .map { it.toDomain() }
                .onSuccess { _overview.value = it }
                .asEmptyResult()
        }
    }

    override fun clear() {
        _overview.value = null
    }
}
