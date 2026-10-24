package com.laarasoft.moepay.home.domain

import com.laarasoft.moepay.core.domain.util.DataError
import com.laarasoft.moepay.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.StateFlow

/** Cached Home dashboard: shown instantly on return, refreshed in the background. */
interface HomeRepository {
    val dashboard: StateFlow<HomeDashboard?>
    suspend fun refresh(): EmptyResult<DataError.Remote>
}
