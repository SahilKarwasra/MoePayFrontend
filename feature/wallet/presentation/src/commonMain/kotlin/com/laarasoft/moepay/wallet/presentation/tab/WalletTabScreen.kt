package com.laarasoft.moepay.wallet.presentation.tab

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.core.designsystem.components.MoeAvatar
import com.laarasoft.moepay.core.designsystem.components.MoeBalanceCard
import com.laarasoft.moepay.core.designsystem.components.MoeBanner
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeButtonStyle
import com.laarasoft.moepay.core.designsystem.components.MoeCard
import com.laarasoft.moepay.core.designsystem.components.MoeCardAction
import com.laarasoft.moepay.core.designsystem.components.MoeEmptyState
import com.laarasoft.moepay.core.designsystem.components.MoeIconTile
import com.laarasoft.moepay.core.designsystem.components.MoeLargeTitle
import com.laarasoft.moepay.core.designsystem.components.MoeListRow
import com.laarasoft.moepay.core.designsystem.components.MoePillTone
import com.laarasoft.moepay.core.designsystem.components.MoeRowDivider
import com.laarasoft.moepay.core.designsystem.components.MoeSectionHeader
import com.laarasoft.moepay.core.designsystem.components.MoeSkeletonRows
import com.laarasoft.moepay.core.designsystem.components.MoeStatusPill
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.native.LocalBottomBarInset
import com.laarasoft.moepay.core.designsystem.motion.fadeUpOnAppear
import com.laarasoft.moepay.core.designsystem.native.MoeAlertDialog
import com.laarasoft.moepay.core.designsystem.native.MoeDialogButton
import com.laarasoft.moepay.core.designsystem.native.MoeDialogButtonStyle
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.domain.wallet.WalletActionId
import com.laarasoft.moepay.core.domain.wallet.WalletOverview
import com.laarasoft.moepay.core.presentation.util.ObserveAsEvents
import com.laarasoft.moepay.core.presentation.util.formatTime
import com.laarasoft.moepay.core.ui.TransactionItem
import com.laarasoft.moepay.wallet.presentation.resources.Res
import com.laarasoft.moepay.wallet.presentation.resources.add_money
import com.laarasoft.moepay.wallet.presentation.resources.balance
import com.laarasoft.moepay.wallet.presentation.resources.bank_accounts
import com.laarasoft.moepay.wallet.presentation.resources.can_add_more
import com.laarasoft.moepay.wallet.presentation.resources.cancel
import com.laarasoft.moepay.wallet.presentation.resources.ewallet
import com.laarasoft.moepay.wallet.presentation.resources.history
import com.laarasoft.moepay.wallet.presentation.resources.limits
import com.laarasoft.moepay.wallet.presentation.resources.link_bank_account
import com.laarasoft.moepay.wallet.presentation.resources.link_bank_subtitle
import com.laarasoft.moepay.wallet.presentation.resources.load_error_title
import com.laarasoft.moepay.wallet.presentation.resources.my_qr
import com.laarasoft.moepay.wallet.presentation.resources.no_activity
import com.laarasoft.moepay.wallet.presentation.resources.per_payment
import com.laarasoft.moepay.wallet.presentation.resources.pin_locked_until
import com.laarasoft.moepay.wallet.presentation.resources.pin_not_set
import com.laarasoft.moepay.wallet.presentation.resources.pin_on
import com.laarasoft.moepay.wallet.presentation.resources.primary
import com.laarasoft.moepay.wallet.presentation.resources.profile
import com.laarasoft.moepay.wallet.presentation.resources.recent_activity
import com.laarasoft.moepay.wallet.presentation.resources.remove
import com.laarasoft.moepay.wallet.presentation.resources.remove_account_message
import com.laarasoft.moepay.wallet.presentation.resources.remove_account_title
import com.laarasoft.moepay.wallet.presentation.resources.requests
import com.laarasoft.moepay.wallet.presentation.resources.requests_none
import com.laarasoft.moepay.wallet.presentation.resources.requests_summary
import com.laarasoft.moepay.wallet.presentation.resources.reset_pin
import com.laarasoft.moepay.wallet.presentation.resources.reset_pin_subtitle
import com.laarasoft.moepay.wallet.presentation.resources.resets_at
import com.laarasoft.moepay.wallet.presentation.resources.retry
import com.laarasoft.moepay.wallet.presentation.resources.security
import com.laarasoft.moepay.wallet.presentation.resources.see_all
import com.laarasoft.moepay.wallet.presentation.resources.sent_today
import com.laarasoft.moepay.wallet.presentation.resources.set_pin_message
import com.laarasoft.moepay.wallet.presentation.resources.set_pin_title
import com.laarasoft.moepay.wallet.presentation.resources.toggle_balance
import com.laarasoft.moepay.wallet.presentation.resources.used_of_limit
import com.laarasoft.moepay.wallet.presentation.resources.wallet_maximum
import com.laarasoft.moepay.wallet.presentation.resources.wallet_pin
import com.laarasoft.moepay.wallet.presentation.resources.withdraw
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WalletTabRoot(
    onNavigate: (WalletDestination) -> Unit,
    viewModel: WalletTabViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.onAction(WalletTabAction.OnAppear) }
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is WalletTabEvent.Navigate -> onNavigate(event.destination)
        }
    }
    WalletTabScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletTabScreen(
    state: WalletTabState,
    onAction: (WalletTabAction) -> Unit,
) {
    val overview = state.overview

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onAction(WalletTabAction.OnRefresh) },
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp + LocalBottomBarInset.current),
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars),
        ) {
            item(key = "title") {
                MoeLargeTitle(
                    title = stringResource(Res.string.ewallet),
                    modifier = Modifier.fadeUpOnAppear(0),
                    trailing = {
                        Box(Modifier.clip(CircleShape).clickable(onClickLabel = stringResource(Res.string.profile)) { onAction(WalletTabAction.OnProfileClick) }) {
                            MoeAvatar(name = state.displayName, size = 40.dp, inverted = true)
                        }
                    },
                )
            }

            if (overview == null && state.error != null) {
                item(key = "error") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 80.dp)) {
                        MoeEmptyState(icon = MoeIcon.Error, title = stringResource(Res.string.load_error_title), message = state.error.asString())
                        Spacer(Modifier.height(20.dp))
                        MoeButton(text = stringResource(Res.string.retry), onClick = { onAction(WalletTabAction.OnRefresh) }, style = MoeButtonStyle.SECONDARY)
                    }
                }
                return@LazyColumn
            }

            item(key = "balance") {
                MoeBalanceCard(
                    label = stringResource(Res.string.balance),
                    balancePence = overview?.wallet?.balance?.pence,
                    balanceDisplay = overview?.wallet?.balance?.display,
                    isHidden = state.isBalanceHidden,
                    onToggleHidden = { onAction(WalletTabAction.OnToggleBalance) },
                    toggleDescription = stringResource(Res.string.toggle_balance),
                    modifier = Modifier.padding(horizontal = 20.dp).fadeUpOnAppear(1),
                ) {
                    Spacer(Modifier.height(22.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        val enabled = { id: WalletActionId -> overview?.actions?.firstOrNull { it.id == id }?.isEnabled ?: false }
                        MoeCardAction(MoeIcon.Add, stringResource(Res.string.add_money), { onAction(WalletTabAction.OnAddMoneyClick) }, enabled = enabled(WalletActionId.ADD_MONEY))
                        MoeCardAction(MoeIcon.Withdraw, stringResource(Res.string.withdraw), { onAction(WalletTabAction.OnWithdrawClick) }, enabled = enabled(WalletActionId.WITHDRAW))
                        MoeCardAction(MoeIcon.Qr, stringResource(Res.string.my_qr), { onAction(WalletTabAction.OnMyQrClick) }, enabled = overview != null)
                        MoeCardAction(MoeIcon.Activity, stringResource(Res.string.history), { onAction(WalletTabAction.OnHistoryClick) }, enabled = overview != null)
                    }
                }
            }

            if (overview?.wallet?.pin?.isSet == false) {
                item(key = "set-pin") {
                    MoeBanner(
                        title = stringResource(Res.string.set_pin_title),
                        message = stringResource(Res.string.set_pin_message),
                        icon = MoeIcon.Lock,
                        onClick = { onAction(WalletTabAction.OnSetPinClick) },
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp),
                    )
                }
            }

            if (overview == null) {
                item(key = "loading") { MoeCard(Modifier.padding(20.dp)) { MoeSkeletonRows(count = 4) } }
                return@LazyColumn
            }

            item(key = "requests") { RequestsSection(overview, onAction) }
            item(key = "activity") { ActivitySection(overview, onAction) }
        }
    }
}

@Composable
private fun RequestsSection(overview: WalletOverview, onAction: (WalletTabAction) -> Unit) {
    val counts = overview.requests
    Column(Modifier.padding(horizontal = 20.dp)) {
        MoeCard(Modifier.padding(top = 24.dp)) {
            MoeListRow(
                title = stringResource(Res.string.requests),
                subtitle = if (counts.incomingPending + counts.outgoingPending == 0) {
                    stringResource(Res.string.requests_none)
                } else {
                    stringResource(Res.string.requests_summary, counts.incomingPending, counts.outgoingPending)
                },
                leading = { MoeIconTile(MoeIcon.Pending) },
                showChevron = true,
                onClick = { onAction(WalletTabAction.OnRequestsClick) },
            )
        }
    }
}

@Composable
private fun ActivitySection(overview: WalletOverview, onAction: (WalletTabAction) -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        MoeSectionHeader(
            title = stringResource(Res.string.recent_activity),
            actionLabel = stringResource(Res.string.see_all),
            onAction = { onAction(WalletTabAction.OnHistoryClick) },
            modifier = Modifier.padding(top = 24.dp),
        )
        MoeCard(Modifier.padding(top = 8.dp)) {
            if (overview.recentTransactions.isEmpty()) {
                Text(
                    stringResource(Res.string.no_activity),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.extended.textSecondary,
                    modifier = Modifier.padding(20.dp),
                )
            }
            overview.recentTransactions.forEachIndexed { index, transaction ->
                if (index > 0) MoeRowDivider()
                TransactionItem(transaction = transaction, onClick = { onAction(WalletTabAction.OnTransactionClick(transaction.id)) })
            }
        }
    }
}
