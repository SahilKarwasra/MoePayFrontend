package com.laarasoft.moepay.home.domain

import com.laarasoft.moepay.core.domain.money.RecentRecipient
import com.laarasoft.moepay.core.domain.money.RequestCounts
import com.laarasoft.moepay.core.domain.money.Transaction
import com.laarasoft.moepay.core.domain.wallet.WalletAction
import com.laarasoft.moepay.core.domain.wallet.WalletSummary

data class HomeUser(
    val id: String,
    val displayName: String,
    val username: String?,
)

/** Everything on the Home tab, from `GET /home`. */
data class HomeDashboard(
    val user: HomeUser,
    /** False until onboarding has opened the wallet. */
    val isWalletReady: Boolean,
    val wallet: WalletSummary?,
    val quickActions: List<WalletAction>,
    val recentRecipients: List<RecentRecipient>,
    val requests: RequestCounts,
    val recentTransactions: List<Transaction>,
)
