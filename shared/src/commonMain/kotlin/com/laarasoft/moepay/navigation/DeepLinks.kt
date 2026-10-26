package com.laarasoft.moepay.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Links opened from outside the app (Android intents, iOS `onOpenURL`). Held until the signed-in
 * app is on screen, then consumed once.
 */
object DeepLinks {
    private const val PAY_PREFIX = "moepay://pay"
    private const val CHAT_PREFIX = "moepay://chat"

    private val _pending = MutableStateFlow<String?>(null)
    val pending: StateFlow<String?> = _pending.asStateFlow()

    fun onNewUri(uri: String) {
        if (uri.startsWith(PAY_PREFIX) || uri.startsWith(CHAT_PREFIX)) _pending.value = uri
    }

    fun consume(): String? = _pending.value.also { _pending.value = null }

    fun isChat(uri: String): Boolean = uri.startsWith(CHAT_PREFIX)

    /** The `u` (username) parameter of `moepay://chat?u=alex`. */
    fun chatUsername(uri: String): String? =
        uri.substringAfter('?', "").split('&')
            .firstOrNull { it.startsWith("u=") }
            ?.removePrefix("u=")
            ?.takeIf { it.isNotBlank() }
}
