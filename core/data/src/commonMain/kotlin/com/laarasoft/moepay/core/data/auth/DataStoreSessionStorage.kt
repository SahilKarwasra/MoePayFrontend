package com.laarasoft.moepay.core.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.laarasoft.moepay.core.data.dto.StoredSession
import com.laarasoft.moepay.core.data.mappers.toDomain
import com.laarasoft.moepay.core.data.mappers.toStored
import com.laarasoft.moepay.core.domain.auth.AuthInfo
import com.laarasoft.moepay.core.domain.auth.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class DataStoreSessionStorage(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
) : SessionStorage {

    private val sessionKey = stringPreferencesKey("KEY_SESSION")

    override fun observeAuthInfo(): Flow<AuthInfo?> {
        return dataStore.data
            .map { preferences ->
                preferences[sessionKey]?.let { serialized ->
                    runCatching { json.decodeFromString<StoredSession>(serialized).toDomain() }.getOrNull()
                }
            }
            .distinctUntilChanged()
    }

    override suspend fun set(info: AuthInfo?) {
        dataStore.edit { preferences ->
            if (info == null) {
                preferences.remove(sessionKey)
            } else {
                preferences[sessionKey] = json.encodeToString(info.toStored())
            }
        }
    }
}
