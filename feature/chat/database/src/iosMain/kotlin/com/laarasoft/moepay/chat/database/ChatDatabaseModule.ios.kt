package com.laarasoft.moepay.chat.database

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

internal actual val platformChatDatabaseModule: Module = module {
    single<RoomDatabase.Builder<ChatDatabase>> {
        Room.databaseBuilder<ChatDatabase>(name = databasePath())
    }
}

/** Application Support: private to the app, backed up, never shown in the Files app. */
@OptIn(ExperimentalForeignApi::class)
private fun databasePath(): String {
    val directory: NSURL = requireNotNull(
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )
    )
    return requireNotNull(directory.path) + "/" + ChatDatabase.FILE_NAME
}
