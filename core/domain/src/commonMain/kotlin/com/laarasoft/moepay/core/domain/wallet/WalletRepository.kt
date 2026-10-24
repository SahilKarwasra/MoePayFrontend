package com.laarasoft.moepay.core.domain.wallet

import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.StateFlow

/**
 * The single source of truth for the wallet across tabs: balance, PIN status, limits and
 * bank accounts. Screens observe [overview]; anything that moves money calls [refresh].
 */
interface WalletRepository {
    val overview: StateFlow<WalletOverview?>
    suspend fun refresh(): EmptyResult<DataError.Remote>
    fun clear()
}
