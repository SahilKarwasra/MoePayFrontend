package com.laarasoft.moepay.home.data

import com.laarasoft.moepay.core.data.di.ApplicationScope
import com.laarasoft.moepay.core.data.money.toDomain
import com.laarasoft.moepay.core.data.money.toRecentRecipient
import com.laarasoft.moepay.core.data.networking.get
import com.laarasoft.moepay.core.domain.auth.SessionStorage
import com.laarasoft.moepay.core.domain.realtime.RealtimeEvents
import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.EmptyResult
import com.laarasoft.moepay.core.domain.util.asEmptyResult
import com.laarasoft.moepay.core.domain.util.map
import com.laarasoft.moepay.core.domain.util.onSuccess
import com.laarasoft.moepay.home.domain.HomeDashboard
import com.laarasoft.moepay.home.domain.HomeRepository
import com.laarasoft.moepay.home.domain.HomeUser
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class KtorHomeRepository(
    private val httpClient: HttpClient,
    realtimeEvents: RealtimeEvents,
    sessionStorage: SessionStorage,
    applicationScope: ApplicationScope,
) : HomeRepository {

    private val _dashboard = MutableStateFlow<HomeDashboard?>(null)
    override val dashboard: StateFlow<HomeDashboard?> = _dashboard.asStateFlow()

    init {
        realtimeEvents.events
            .onEach { refresh() }
            .launchIn(applicationScope)
        sessionStorage.observeAuthInfo()
            .onEach { if (it == null) _dashboard.value = null }
            .launchIn(applicationScope)
    }

    override suspend fun refresh(): EmptyResult<DataError.Remote> {
        return httpClient.get<HomeDto>(route = "/home")
            .map { it.toDomain() }
            .onSuccess { _dashboard.value = it }
            .asEmptyResult()
    }
}

private fun HomeDto.toDomain(): HomeDashboard = HomeDashboard(
    user = HomeUser(id = me.id, displayName = me.displayName, username = me.username),
    isWalletReady = walletReady,
    wallet = wallet?.toDomain(),
    quickActions = quickActions.map { it.toDomain() },
    recentRecipients = recentRecipients.map { it.toRecentRecipient() },
    requests = requests.toDomain(),
    recentTransactions = recentTransactions.map { it.toDomain() },
)
