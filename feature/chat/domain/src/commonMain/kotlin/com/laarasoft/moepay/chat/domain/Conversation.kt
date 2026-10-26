package com.laarasoft.moepay.chat.domain

import kotlin.time.Instant

/** The other member of a one-to-one conversation. */
data class Peer(
    val userId: String,
    val username: String?,
    val displayName: String,
    val avatarUrl: String?,
)

data class Conversation(
    val id: String,
    val peer: Peer,
    /** Highest server `seq` in the conversation. */
    val lastSeq: Long,
    /** Newest message, including ones still waiting to be sent. */
    val lastMessage: ChatMessage?,
    val lastActivityAt: Instant,
    val unreadCount: Int,
    val myLastReadSeq: Long,
    /** The peer has read every message up to this seq: drives the "read" ticks. */
    val peerLastReadSeq: Long,
    /** Unsent text typed into the composer, kept on the device. */
    val draft: String = "",
)
