package com.laarasoft.moepay.core.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.laarasoft.moepay.core.data.session.SecureStore
import com.laarasoft.moepay.core.domain.auth.AuthInfo
import com.laarasoft.moepay.core.domain.auth.User
import com.laarasoft.moepay.core.domain.onboarding.OnboardingStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeSecureStore : SecureStore {
    val values = mutableMapOf<String, String>()
    override suspend fun get(key: String) = values[key]
    override suspend fun set(key: String, value: String) { values[key] = value }
    override suspend fun remove(key: String) { values.remove(key) }
}

private class FakeDataStore(initial: Preferences = emptyPreferences()) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)
    override val data: Flow<Preferences> = state
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        transform(state.value).also { state.value = it }
}

class SecureSessionStorageTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val session = AuthInfo(
        accessToken = "access",
        refreshToken = "refresh",
        user = User("u1", "+447911123456", "alex", "Alex", "Taylor", null),
        onboardingStep = OnboardingStep.COMPLETED,
    )
    private val legacyJson = """
        {"accessToken":"access","refreshToken":"refresh","userId":"u1","phoneNumber":"+447911123456",
         "username":"alex","firstName":"Alex","lastName":"Taylor","onboardingStep":"COMPLETED"}
    """.trimIndent()

    @Test
    fun migratesThePlaintextSessionIntoSecureStorageAndDeletesIt() = runTest {
        val secure = FakeSecureStore()
        val legacy = FakeDataStore(mutablePreferencesOf(stringPreferencesKey("KEY_SESSION") to legacyJson))
        val storage = SecureSessionStorage(secure, legacy, json)

        assertEquals(session, storage.observeAuthInfo().first())
        assertTrue(secure.values.containsKey("session"))
        assertNull(legacy.data.first()[stringPreferencesKey("KEY_SESSION")])
    }

    @Test
    fun setAndClearGoThroughSecureStorage() = runTest {
        val secure = FakeSecureStore()
        val storage = SecureSessionStorage(secure, FakeDataStore(), json)

        storage.set(session)
        assertEquals(session, storage.observeAuthInfo().first())
        assertTrue(secure.values.getValue("session").contains("refresh"))

        storage.set(null)
        assertNull(storage.observeAuthInfo().first())
        assertTrue(secure.values.isEmpty())
    }
}
