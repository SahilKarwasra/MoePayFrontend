package com.laarasoft.moepay.core.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.laarasoft.moepay.core.data.dto.StoredSession
import com.laarasoft.moepay.core.data.mappers.toDomain
import com.laarasoft.moepay.core.data.mappers.toStored
import com.laarasoft.moepay.core.data.session.SecureStore
import com.laarasoft.moepay.core.domain.auth.AuthInfo
import com.laarasoft.moepay.core.domain.auth.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

private const val SECURE_SESSION_KEY = "session"

/** Where earlier builds kept the session in plain text; read once to migrate, then deleted. */
private val legacySessionKey = stringPreferencesKey("KEY_SESSION")

/**
 * Session tokens live in [SecureStore]. An in-memory copy backs [observeAuthInfo] so every
 * screen sees changes immediately.
 */
class SecureSessionStorage(
    private val secureStore: SecureStore,
    private val legacyDataStore: DataStore<Preferences>,
    private val json: Json,
) : SessionStorage {

    private val loadMutex = Mutex()
    private var isLoaded = false
    private val session = MutableStateFlow<AuthInfo?>(null)

    override fun observeAuthInfo(): Flow<AuthInfo?> = flow {
        ensureLoaded()
        emitAll(session)
    }.distinctUntilChanged()

    override suspend fun set(info: AuthInfo?) {
        ensureLoaded()
        if (info == null) {
            secureStore.remove(SECURE_SESSION_KEY)
        } else {
            secureStore.set(SECURE_SESSION_KEY, json.encodeToString(info.toStored()))
        }
        session.value = info
    }

    private suspend fun ensureLoaded() {
        if (isLoaded) return
        loadMutex.withLock {
            if (isLoaded) return
            session.value = readSecure() ?: migrateLegacy()
            isLoaded = true
        }
    }

    private suspend fun readSecure(): AuthInfo? =
        secureStore.get(SECURE_SESSION_KEY)?.let(::decode)

    private suspend fun migrateLegacy(): AuthInfo? {
        val legacy = legacyDataStore.data.first()[legacySessionKey] ?: return null
        val info = decode(legacy)
        if (info != null) secureStore.set(SECURE_SESSION_KEY, legacy)
        legacyDataStore.edit { it.remove(legacySessionKey) }
        return info
    }

    private fun decode(serialized: String): AuthInfo? =
        runCatching { json.decodeFromString<StoredSession>(serialized).toDomain() }.getOrNull()
}
