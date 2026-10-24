package com.laarasoft.moepay.core.data.session

/**
 * Small secrets kept in the platform's protected storage:
 * iOS Keychain (this device only, after first unlock), Android AES-GCM with a Keystore key.
 */
interface SecureStore {
    suspend fun get(key: String): String?
    suspend fun set(key: String, value: String)
    suspend fun remove(key: String)
}
