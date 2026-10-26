package com.laarasoft.moepay.chat.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.laarasoft.moepay.chat.domain.ChatLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

/** Platform part: where the database file lives. */
internal expect val platformChatDatabaseModule: Module

val chatDatabaseModule = module {
    includes(platformChatDatabaseModule)
    single {
        get<RoomDatabase.Builder<ChatDatabase>>()
            // The same bundled SQLite on Android and iOS.
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
    single { RoomChatLocalDataSource(get()) } bind ChatLocalDataSource::class
}
