package com.laarasoft.moepay.core.domain.preferences

import kotlinx.coroutines.flow.Flow

/** Small per-device UI preferences. */
interface AppPreferences {
    fun observeBalanceHidden(): Flow<Boolean>
    suspend fun setBalanceHidden(hidden: Boolean)
}
