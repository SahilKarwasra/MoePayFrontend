package com.laarasoft.moepay.home.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.core.designsystem.components.MoeAvatar
import com.laarasoft.moepay.core.designsystem.components.MoeBalanceCard
import com.laarasoft.moepay.core.designsystem.components.MoeBanner
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeButtonStyle
import com.laarasoft.moepay.core.designsystem.components.MoeCard
import com.laarasoft.moepay.core.designsystem.components.MoeEmptyState
import com.laarasoft.moepay.core.designsystem.components.MoeQuickAction
import com.laarasoft.moepay.core.designsystem.components.MoeRowDivider
import com.laarasoft.moepay.core.designsystem.components.MoeSectionHeader
import com.laarasoft.moepay.core.designsystem.components.MoeSkeletonBlock
import com.laarasoft.moepay.core.designsystem.components.MoeSkeletonRows
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.native.LocalBottomBarInset
import com.laarasoft.moepay.core.designsystem.motion.fadeUpOnAppear
import com.laarasoft.moepay.core.designsystem.native.MoeAlertDialog
import com.laarasoft.moepay.core.designsystem.native.MoeDialogButton
import com.laarasoft.moepay.core.designsystem.theme.MoeTheme
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.domain.wallet.ActionBlocker
import com.laarasoft.moepay.core.domain.wallet.WalletActionId
import com.laarasoft.moepay.core.presentation.util.ObserveAsEvents
import com.laarasoft.moepay.core.ui.TransactionItem
import com.laarasoft.moepay.core.ui.icon
import com.laarasoft.moepay.core.ui.label
import com.laarasoft.moepay.home.presentation.resources.Res
import com.laarasoft.moepay.home.presentation.resources.balance
import com.laarasoft.moepay.home.presentation.resources.blocker_not_ready_message
import com.laarasoft.moepay.home.presentation.resources.blocker_not_ready_title
import com.laarasoft.moepay.home.presentation.resources.blocker_wallet_frozen_message
import com.laarasoft.moepay.home.presentation.resources.blocker_wallet_frozen_title
import com.laarasoft.moepay.home.presentation.resources.greeting_afternoon
import com.laarasoft.moepay.home.presentation.resources.greeting_evening
import com.laarasoft.moepay.home.presentation.resources.greeting_morning
import com.laarasoft.moepay.home.presentation.resources.load_error_title
import com.laarasoft.moepay.home.presentation.resources.no_activity
import com.laarasoft.moepay.home.presentation.resources.ok
import com.laarasoft.moepay.home.presentation.resources.people
import com.laarasoft.moepay.home.presentation.resources.recent_activity
import com.laarasoft.moepay.home.presentation.resources.requests_message
import com.laarasoft.moepay.home.presentation.resources.requests_title_many
import com.laarasoft.moepay.home.presentation.resources.requests_title_one
import com.laarasoft.moepay.home.presentation.resources.retry
import com.laarasoft.moepay.home.presentation.resources.see_all
import com.laarasoft.moepay.home.presentation.resources.set_pin_message
import com.laarasoft.moepay.home.presentation.resources.set_pin_title
import com.laarasoft.moepay.home.presentation.resources.toggle_balance
import com.laarasoft.moepay.home.presentation.resources.wallet_setting_up_message
import com.laarasoft.moepay.home.presentation.resources.wallet_setting_up_title
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
fun HomeRoot(
    onNavigate: (HomeDestination) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is HomeEvent.Navigate -> onNavigate(event.destination)
        }
    }
    HomeScreen(state = state, onAction = viewModel::onAction)
}

/** Quick actions shown on Home, in the server's order. The rest live on the eWallet tab. */
private val homeActionIds = setOf(WalletActionId.SEND, WalletActionId.REQUEST, WalletActionId.SCAN_QR, WalletActionId.ADD_MONEY)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
) {
    val dashboard = state.dashboard

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onAction(HomeAction.OnRefresh) },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp + LocalBottomBarInset.current),
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            item(key = "header") {
                Header(
                    name = dashboard?.user?.displayName,
                    onAvatarClick = { onAction(HomeAction.OnAvatarClick) },
                    modifier = Modifier.fadeUpOnAppear(0),
                )
            }

            if (dashboard == null && state.error != null) {
                item(key = "error") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(top = 96.dp),
                    ) {
                        MoeEmptyState(icon = MoeIcon.Error, title = stringResource(Res.string.load_error_title), message = state.error.asString())
                        Spacer(Modifier.height(20.dp))
                        MoeButton(text = stringResource(Res.string.retry), onClick = { onAction(HomeAction.OnRefresh) }, style = MoeButtonStyle.SECONDARY)
                    }
                }
                return@LazyColumn
            }

            item(key = "balance") {
                MoeBalanceCard(
                    label = stringResource(Res.string.balance),
                    balancePence = dashboard?.wallet?.balance?.pence ?: dashboard?.let { 0L },
                    balanceDisplay = dashboard?.wallet?.balance?.display ?: dashboard?.let { "£0.00" },
                    isHidden = state.isBalanceHidden,
                    onToggleHidden = { onAction(HomeAction.OnToggleBalance) },
                    toggleDescription = stringResource(Res.string.toggle_balance),
                    modifier = Modifier.padding(horizontal = 20.dp).fadeUpOnAppear(1),
                )
            }

            item(key = "actions") {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 20.dp).fadeUpOnAppear(2),
                ) {
                    val actions = dashboard?.quickActions?.filter { it.id in homeActionIds }
                    if (actions == null) {
                        repeat(4) { MoeSkeletonBlock(width = 56.dp, height = 56.dp, shape = CircleShape) }
                    } else {
                        actions.forEach { action ->
                            MoeQuickAction(
                                icon = action.id.icon,
                                label = action.id.label(),
                                enabled = action.isEnabled,
                                onClick = { onAction(HomeAction.OnQuickActionClick(action)) },
                            )
                        }
                    }
                }
            }

            if (dashboard != null && !dashboard.isWalletReady) {
                item(key = "setting-up") {
                    MoeBanner(
                        title = stringResource(Res.string.wallet_setting_up_title),
                        message = stringResource(Res.string.wallet_setting_up_message),
                        icon = MoeIcon.Pending,
                        onClick = { onAction(HomeAction.OnRefresh) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    )
                }
            }
            if (dashboard?.wallet?.pin?.isSet == false) {
                item(key = "set-pin") {
                    MoeBanner(
                        title = stringResource(Res.string.set_pin_title),
                        message = stringResource(Res.string.set_pin_message),
                        icon = MoeIcon.Lock,
                        onClick = { onAction(HomeAction.OnSetPinClick) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp).fadeUpOnAppear(3),
                    )
                }
            }
            val waiting = dashboard?.requests?.incomingPending ?: 0
            if (waiting > 0) {
                item(key = "requests") {
                    MoeBanner(
                        title = if (waiting == 1) stringResource(Res.string.requests_title_one) else stringResource(Res.string.requests_title_many, waiting),
                        message = stringResource(Res.string.requests_message),
                        icon = MoeIcon.Pending,
                        onClick = { onAction(HomeAction.OnRequestsClick) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp).fadeUpOnAppear(3),
                    )
                }
            }

            val recipients = dashboard?.recentRecipients.orEmpty()
            if (recipients.isNotEmpty()) {
                item(key = "people-header") {
                    MoeSectionHeader(title = stringResource(Res.string.people), modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp))
                }
                item(key = "people") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        items(recipients, key = { it.recipient.userId }) { recent ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(64.dp)
                                    .clickable { onAction(HomeAction.OnRecipientClick(recent.recipient)) },
                            ) {
                                MoeAvatar(name = recent.recipient.displayName, size = 56.dp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = recent.recipient.displayName.substringBefore(' '),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }

            item(key = "activity-header") {
                MoeSectionHeader(
                    title = stringResource(Res.string.recent_activity),
                    actionLabel = stringResource(Res.string.see_all),
                    onAction = { onAction(HomeAction.OnSeeAllActivityClick) }.takeIf { dashboard?.isWalletReady == true },
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp),
                )
            }
            item(key = "activity") {
                MoeCard(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    val transactions = dashboard?.recentTransactions
                    when {
                        transactions == null -> MoeSkeletonRows(count = 3)
                        transactions.isEmpty() -> Text(
                            text = stringResource(Res.string.no_activity),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.extended.textSecondary,
                            modifier = Modifier.padding(20.dp),
                        )
                        else -> transactions.forEachIndexed { index, transaction ->
                            if (index > 0) MoeRowDivider()
                            TransactionItem(transaction = transaction, onClick = { onAction(HomeAction.OnTransactionClick(transaction.id)) })
                        }
                    }
                }
            }
        }
    }


    state.blocker?.let { blocker ->
        val (title, message) = when (blocker) {
            ActionBlocker.WALLET_FROZEN -> stringResource(Res.string.blocker_wallet_frozen_title) to stringResource(Res.string.blocker_wallet_frozen_message)
            else -> stringResource(Res.string.blocker_not_ready_title) to stringResource(Res.string.blocker_not_ready_message)
        }
        MoeAlertDialog(
            title = title,
            message = message,
            confirmButton = MoeDialogButton(text = stringResource(Res.string.ok), onClick = { onAction(HomeAction.OnDismissBlocker) }),
            onDismissRequest = { onAction(HomeAction.OnDismissBlocker) },
        )
    }
}

@Composable
private fun Header(
    name: String?,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    val greeting = when (hour) {
        in 5..11 -> stringResource(Res.string.greeting_morning)
        in 12..17 -> stringResource(Res.string.greeting_afternoon)
        else -> stringResource(Res.string.greeting_evening)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = greeting, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.extended.textSecondary)
            if (name != null) {
                Text(
                    text = name.substringBefore(' '),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                MoeSkeletonBlock(width = 140.dp, height = 32.dp, modifier = Modifier.padding(top = 4.dp))
            }
        }
        Box(modifier = Modifier.clickable(onClick = onAvatarClick)) {
            MoeAvatar(name = name ?: "", size = 44.dp, inverted = true)
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    MoeTheme { HomeScreen(state = HomeState(), onAction = {}) }
}
