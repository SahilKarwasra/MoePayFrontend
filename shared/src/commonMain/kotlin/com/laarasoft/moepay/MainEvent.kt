package com.laarasoft.moepay

sealed interface MainEvent {
    /** The session went away (logout, refresh token rejected): return to auth. */
    data object SessionEnded : MainEvent
}
