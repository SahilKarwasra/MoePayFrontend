package com.laarasoft.moepay.home.data

import com.laarasoft.moepay.core.data.money.ActionDto
import com.laarasoft.moepay.core.data.money.RecipientDto
import com.laarasoft.moepay.core.data.money.RequestCountsDto
import com.laarasoft.moepay.core.data.money.TransactionDto
import com.laarasoft.moepay.core.data.money.WalletSummaryDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class HomeMeDto(
    val id: String,
    @SerialName("display_name") val displayName: String,
    val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

@Serializable
internal data class HomeDto(
    val me: HomeMeDto,
    @SerialName("wallet_ready") val walletReady: Boolean,
    val wallet: WalletSummaryDto? = null,
    @SerialName("quick_actions") val quickActions: List<ActionDto> = emptyList(),
    @SerialName("recent_recipients") val recentRecipients: List<RecipientDto> = emptyList(),
    val requests: RequestCountsDto = RequestCountsDto(),
    @SerialName("recent_transactions") val recentTransactions: List<TransactionDto> = emptyList(),
)
