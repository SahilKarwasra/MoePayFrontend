package com.laarasoft.moepay.chat.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.laarasoft.moepay.chat.database.dao.ConversationDao
import com.laarasoft.moepay.chat.database.dao.MessageDao
import com.laarasoft.moepay.chat.database.dao.OutboxDao
import com.laarasoft.moepay.chat.database.dao.SyncStateDao
import com.laarasoft.moepay.chat.database.entity.ConversationEntity
import com.laarasoft.moepay.chat.database.entity.MessageEntity
import com.laarasoft.moepay.chat.database.entity.OutboxEntity
import com.laarasoft.moepay.chat.database.entity.SyncStateEntity

@Database(
    entities = [ConversationEntity::class, MessageEntity::class, OutboxEntity::class, SyncStateEntity::class],
    version = 1,
)
@ConstructedBy(ChatDatabaseConstructor::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract val conversationDao: ConversationDao
    abstract val messageDao: MessageDao
    abstract val outboxDao: OutboxDao
    abstract val syncStateDao: SyncStateDao

    companion object {
        const val FILE_NAME = "moepay_chat.db"
    }
}

// Room generates the actual implementations.
@Suppress("KotlinNoActualForExpect")
expect object ChatDatabaseConstructor : RoomDatabaseConstructor<ChatDatabase> {
    override fun initialize(): ChatDatabase
}
