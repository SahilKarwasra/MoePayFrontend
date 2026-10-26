package com.laarasoft.moepay.chat.domain

/** What the chat UI tells the user about its link to the server. */
enum class ChatConnection {
    /** Live updates are flowing. */
    ONLINE,

    /** The network is up but the live connection isn't (yet). */
    CONNECTING,

    /** No network: everything still works locally and is sent later. */
    OFFLINE,
}
