package com.laarasoft.moepay.chat.presentation.di

import com.laarasoft.moepay.chat.presentation.conversation.ConversationViewModel
import com.laarasoft.moepay.chat.presentation.list.ChatListViewModel
import com.laarasoft.moepay.chat.presentation.money.ChatMoneyViewModel
import com.laarasoft.moepay.chat.presentation.newchat.NewChatViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val chatPresentationModule = module {
    viewModelOf(::ChatListViewModel)
    viewModel { params -> NewChatViewModel(target = params.get(), chatRepository = get()) }
    viewModel { params -> ConversationViewModel(conversationId = params.get(), chatRepository = get(), typingIndicator = get()) }
    viewModel { params -> ChatMoneyViewModel(conversationId = params.get(), chatRepository = get(), walletRepository = get()) }
}
