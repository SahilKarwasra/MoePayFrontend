package com.laarasoft.moepay.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.laarasoft.moepay.core.domain.preferences.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DataStoreAppPreferences(
    private val dataStore: DataStore<Preferences>,
) : AppPreferences {

    private val balanceHiddenKey = booleanPreferencesKey("balance_hidden")

    override fun observeBalanceHidden(): Flow<Boolean> =
        dataStore.data.map { it[balanceHiddenKey] ?: false }.distinctUntilChanged()

    override suspend fun setBalanceHidden(hidden: Boolean) {
        dataStore.edit { it[balanceHiddenKey] = hidden }
    }
}
