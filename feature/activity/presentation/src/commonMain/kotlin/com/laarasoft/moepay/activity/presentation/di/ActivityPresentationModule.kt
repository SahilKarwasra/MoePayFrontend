package com.laarasoft.moepay.activity.presentation.di

import com.laarasoft.moepay.activity.presentation.detail.TransactionDetailViewModel
import com.laarasoft.moepay.activity.presentation.history.HistoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val activityPresentationModule = module {
    viewModelOf(::HistoryViewModel)
    viewModel { params -> TransactionDetailViewModel(transactionId = params.get(), activityService = get()) }
}
