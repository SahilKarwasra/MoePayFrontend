package com.laarasoft.moepay.chat.data

import com.laarasoft.moepay.chat.data.remote.MessageDto
import com.laarasoft.moepay.chat.data.remote.toDomain
import com.laarasoft.moepay.chat.domain.MessageContent
import com.laarasoft.moepay.core.domain.money.MoneyRequestAction
import com.laarasoft.moepay.core.domain.money.MoneyRequestStatus
import com.laarasoft.moepay.core.domain.money.TransactionStatus
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ChatMappersTest {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    private fun decode(text: String) = json.decodeFromString(MessageDto.serializer(), text).toDomain()

    @Test
    fun textMessageKeysOnItsClientId() {
        val message = decode(
            """{"id":"m1","conversation_id":"c1","seq":4,"client_message_id":"cid","sender_id":"u1","mine":true,
               "kind":"text","body":"hi","version":2,"edited":true,"deleted":false,"created_at":"2026-10-26T10:00:00Z"}"""
        )
        assertEquals("cid", message.localId)
        assertEquals("m1", message.serverId)
        assertEquals(MessageContent.Text("hi"), message.content)
        assertEquals(true, message.isEdited)
    }

    @Test
    fun paymentFromTheWalletScreenKeysOnItsServerId() {
        val message = decode(
            """{"id":"m2","conversation_id":"c1","seq":5,"client_message_id":null,"sender_id":"u2","mine":false,"kind":"payment",
               "body":"Dinner","payment":{"transaction_id":"t1","reference":"MP-1","amount":{"amount_pence":1250,"currency":"GBP","display":"£12.50"},
               "direction":"in","status":"succeeded"},"version":1,"edited":false,"deleted":false,"created_at":"2026-10-26T10:00:00Z"}"""
        )
        assertEquals("m2", message.localId)
        val payment = assertIs<MessageContent.Payment>(message.content)
        assertEquals(1250, payment.amount.pence)
        assertEquals(false, payment.isOutgoing)
        assertEquals(TransactionStatus.SUCCEEDED, payment.status)
        assertEquals("Dinner", payment.note)
    }

    @Test
    fun requestCarriesStatusAndViewerActions() {
        val message = decode(
            """{"id":"m3","conversation_id":"c1","seq":6,"sender_id":"u2","mine":false,"kind":"request","body":null,
               "request":{"money_request_id":"r1","amount":{"amount_pence":500,"currency":"GBP","display":"£5.00"},"direction":"incoming",
               "status":"pending","actions":["pay","decline"],"expires_at":"2026-11-02T10:00:00Z"},
               "version":1,"edited":false,"deleted":false,"created_at":"2026-10-26T10:00:00Z"}"""
        )
        val request = assertIs<MessageContent.Request>(message.content)
        assertEquals(MoneyRequestStatus.PENDING, request.status)
        assertEquals(setOf(MoneyRequestAction.PAY, MoneyRequestAction.DECLINE), request.actions)
        assertEquals(false, request.isOutgoing)
    }

    @Test
    fun deletedMessagesHideTheirBody() {
        val message = decode(
            """{"id":"m4","conversation_id":"c1","seq":7,"sender_id":"u1","mine":true,"kind":"text","body":null,
               "version":3,"edited":false,"deleted":true,"created_at":"2026-10-26T10:00:00Z"}"""
        )
        assertEquals(MessageContent.Deleted, message.content)
    }
}
