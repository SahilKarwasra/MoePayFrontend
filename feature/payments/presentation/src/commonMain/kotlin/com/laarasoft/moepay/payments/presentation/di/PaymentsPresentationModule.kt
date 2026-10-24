package com.laarasoft.moepay.payments.presentation.di

import com.laarasoft.moepay.payments.presentation.requests.MoneyRequestsViewModel
import com.laarasoft.moepay.payments.presentation.requests.RequestDetailViewModel
import com.laarasoft.moepay.payments.presentation.transfer.TransferFlowViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val paymentsPresentationModule = module {
    viewModelOf(::MoneyRequestsViewModel)
    viewModel { params -> TransferFlowViewModel(args = params.get(), paymentsService = get(), walletRepository = get()) }
    viewModel { params -> RequestDetailViewModel(requestId = params.get(), paymentsService = get(), realtimeEvents = get()) }
}
