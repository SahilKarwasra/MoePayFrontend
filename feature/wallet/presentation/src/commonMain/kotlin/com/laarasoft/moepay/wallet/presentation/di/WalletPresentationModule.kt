package com.laarasoft.moepay.wallet.presentation.di

import com.laarasoft.moepay.wallet.presentation.bank.LinkBankViewModel
import com.laarasoft.moepay.wallet.presentation.pin.PinFlowViewModel
import com.laarasoft.moepay.wallet.presentation.profile.ProfileViewModel
import com.laarasoft.moepay.wallet.presentation.qr.MyQrViewModel
import com.laarasoft.moepay.wallet.presentation.tab.WalletTabViewModel
import com.laarasoft.moepay.wallet.presentation.transfer.BankTransferViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val walletPresentationModule = module {
    viewModelOf(::WalletTabViewModel)
    viewModelOf(::LinkBankViewModel)
    viewModelOf(::MyQrViewModel)
    viewModelOf(::ProfileViewModel)
    viewModel { params -> PinFlowViewModel(mode = params.get(), walletService = get()) }
    viewModel { params ->
        BankTransferViewModel(direction = params.get(), walletRepository = get(), walletService = get())
    }
}
