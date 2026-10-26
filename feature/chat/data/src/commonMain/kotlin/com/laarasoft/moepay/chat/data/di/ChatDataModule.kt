package com.laarasoft.moepay.chat.data.di

import com.laarasoft.moepay.chat.data.OfflineFirstChatRepository
import com.laarasoft.moepay.chat.data.remote.ChatRemoteDataSource
import com.laarasoft.moepay.chat.data.remote.KtorChatRemoteDataSource
import com.laarasoft.moepay.chat.data.sync.ChatSyncEngine
import com.laarasoft.moepay.chat.data.sync.DefaultChatEngine
import com.laarasoft.moepay.chat.data.sync.OutboxProcessor
import com.laarasoft.moepay.chat.data.sync.RealtimeTypingIndicator
import com.laarasoft.moepay.chat.database.chatDatabaseModule
import com.laarasoft.moepay.chat.domain.ChatEngine
import com.laarasoft.moepay.chat.domain.ChatRepository
import com.laarasoft.moepay.chat.domain.TypingIndicator
import org.koin.dsl.binds
import org.koin.dsl.module

val chatDataModule = module {
    includes(chatDatabaseModule)
    single<ChatRemoteDataSource> { KtorChatRemoteDataSource(get()) }
    single { ChatSyncEngine(get(), get()) }
    single { OutboxProcessor(get(), get(), get()) }
    single { RealtimeTypingIndicator(get(), get()) } binds arrayOf(TypingIndicator::class)
    single {
        DefaultChatEngine(
            sync = get(),
            outbox = get(),
            local = get(),
            realtime = get(),
            connectivity = get(),
            typing = get(),
            json = get(),
            applicationScope = get(),
        )
    } binds arrayOf(ChatEngine::class)
    single<ChatRepository> {
        OfflineFirstChatRepository(
            local = get(),
            remote = get(),
            sync = get(),
            engine = get(),
            sessionStorage = get(),
            walletRepository = get(),
            json = get(),
            realtime = get(),
            connectivity = get(),
            applicationScope = get(),
        )
    }
}
