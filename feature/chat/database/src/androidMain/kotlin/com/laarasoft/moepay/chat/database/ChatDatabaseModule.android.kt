package com.laarasoft.moepay.chat.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformChatDatabaseModule: Module = module {
    single<RoomDatabase.Builder<ChatDatabase>> {
        val context = get<Context>().applicationContext
        Room.databaseBuilder<ChatDatabase>(
            context = context,
            name = context.getDatabasePath(ChatDatabase.FILE_NAME).absolutePath,
        )
    }
}
