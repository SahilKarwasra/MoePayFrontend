package com.laarasoft.moepay.wallet.presentation.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.core.designsystem.components.MoeAvatar
import com.laarasoft.moepay.core.designsystem.components.MoeBanner
import com.laarasoft.moepay.core.designsystem.components.MoeCard
import com.laarasoft.moepay.core.designsystem.components.MoeIconTile
import com.laarasoft.moepay.core.designsystem.components.MoeListRow
import com.laarasoft.moepay.core.designsystem.components.MoePillTone
import com.laarasoft.moepay.core.designsystem.components.MoeRowDivider
import com.laarasoft.moepay.core.designsystem.components.MoeSectionHeader
import com.laarasoft.moepay.core.designsystem.components.MoeSkeletonRows
import com.laarasoft.moepay.core.designsystem.components.MoeStatusPill
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.motion.fadeUpOnAppear
import com.laarasoft.moepay.core.designsystem.native.MoeAlertDialog
import com.laarasoft.moepay.core.designsystem.native.MoeBarAction
import com.laarasoft.moepay.core.designsystem.native.MoeDialogButton
import com.laarasoft.moepay.core.designsystem.native.MoeDialogButtonStyle
import com.laarasoft.moepay.core.designsystem.native.MoeTopBar
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.domain.wallet.WalletOverview
import com.laarasoft.moepay.core.presentation.util.ObserveAsEvents
import com.laarasoft.moepay.core.presentation.util.formatPhoneForDisplay
import com.laarasoft.moepay.core.presentation.util.formatTime
import com.laarasoft.moepay.wallet.presentation.resources.Res
import com.laarasoft.moepay.wallet.presentation.resources.back
import com.laarasoft.moepay.wallet.presentation.resources.bank_accounts
import com.laarasoft.moepay.wallet.presentation.resources.can_add_more
import com.laarasoft.moepay.wallet.presentation.resources.cancel
import com.laarasoft.moepay.wallet.presentation.resources.limits
import com.laarasoft.moepay.wallet.presentation.resources.link_bank_account
import com.laarasoft.moepay.wallet.presentation.resources.link_bank_subtitle
import com.laarasoft.moepay.wallet.presentation.resources.logout
import com.laarasoft.moepay.wallet.presentation.resources.logout_dialog_message
import com.laarasoft.moepay.wallet.presentation.resources.logout_dialog_title
import com.laarasoft.moepay.wallet.presentation.resources.per_payment
import com.laarasoft.moepay.wallet.presentation.resources.pin_locked_until
import com.laarasoft.moepay.wallet.presentation.resources.pin_not_set
import com.laarasoft.moepay.wallet.presentation.resources.pin_on
import com.laarasoft.moepay.wallet.presentation.resources.primary
import com.laarasoft.moepay.wallet.presentation.resources.profile
import com.laarasoft.moepay.wallet.presentation.resources.remove
import com.laarasoft.moepay.wallet.presentation.resources.remove_account_message
import com.laarasoft.moepay.wallet.presentation.resources.remove_account_title
import com.laarasoft.moepay.wallet.presentation.resources.reset_pin
import com.laarasoft.moepay.wallet.presentation.resources.reset_pin_subtitle
import com.laarasoft.moepay.wallet.presentation.resources.resets_at
import com.laarasoft.moepay.wallet.presentation.resources.security
import com.laarasoft.moepay.wallet.presentation.resources.sent_today
import com.laarasoft.moepay.wallet.presentation.resources.set_pin_message
import com.laarasoft.moepay.wallet.presentation.resources.set_pin_title
import com.laarasoft.moepay.wallet.presentation.resources.used_of_limit
import com.laarasoft.moepay.wallet.presentation.resources.wallet_maximum
import com.laarasoft.moepay.wallet.presentation.resources.wallet_pin
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileRoot(
    onBack: () -> Unit,
    onSetPin: () -> Unit,
    onChangePin: () -> Unit,
    onResetPin: () -> Unit,
    onLinkBank: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ProfileEvent.Back -> onBack()
            ProfileEvent.SetPin -> onSetPin()
            ProfileEvent.ChangePin -> onChangePin()
            ProfileEvent.ResetPin -> onResetPin()
            ProfileEvent.LinkBank -> onLinkBank()
        }
    }
    ProfileScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
) {
    val overview = state.overview

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        MoeTopBar(
            title = stringResource(Res.string.profile),
            navigationAction = MoeBarAction(MoeIcon.Back, stringResource(Res.string.back)) { onAction(ProfileAction.OnBackClick) },
        )
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(ProfileAction.OnRefresh) },
            modifier = Modifier.weight(1f),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp),
                modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars),
            ) {
                item(key = "header") { ProfileHeader(state, Modifier.fadeUpOnAppear(0)) }

                if (overview?.wallet?.pin?.isSet == false) {
                    item(key = "set-pin") {
                        MoeBanner(
                            title = stringResource(Res.string.set_pin_title),
                            message = stringResource(Res.string.set_pin_message),
                            icon = MoeIcon.Lock,
                            onClick = { onAction(ProfileAction.OnPinClick) },
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
                        )
                    }
                }

                if (overview == null) {
                    item(key = "loading") {
                        MoeCard(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) { MoeSkeletonRows(count = 4) }
                    }
                } else {
                    item(key = "security") { SecuritySection(overview, onAction, Modifier.fadeUpOnAppear(1)) }
                    item(key = "banks") { BanksSection(overview, onAction, Modifier.fadeUpOnAppear(2)) }
                    item(key = "limits") { LimitsSection(overview, Modifier.fadeUpOnAppear(3)) }
                }

                item(key = "logout") {
                    MoeCard(Modifier.padding(start = 20.dp, end = 20.dp, top = 32.dp)) {
                        MoeListRow(
                            title = stringResource(Res.string.logout),
                            titleColor = MaterialTheme.colorScheme.error,
                            leading = { MoeIconTile(MoeIcon.Logout, tint = MaterialTheme.colorScheme.error) },
                            onClick = if (state.isLoggingOut) null else ({ onAction(ProfileAction.OnLogoutClick) }),
                        )
                    }
                }
            }
        }
    }

    BankAccountSheet(
        bank = state.selectedBank,
        isBusy = state.isBankActionInProgress,
        error = state.bankActionError,
        onMakePrimary = { onAction(ProfileAction.OnMakePrimaryClick) },
        onRemove = { onAction(ProfileAction.OnRemoveBankClick) },
        onDismiss = { onAction(ProfileAction.OnDismissBankSheet) },
    )

    state.bankPendingRemoval?.let { bank ->
        MoeAlertDialog(
            title = stringResource(Res.string.remove_account_title, "${bank.bankName} ••${bank.last4}"),
            message = stringResource(Res.string.remove_account_message),
            confirmButton = MoeDialogButton(stringResource(Res.string.remove), { onAction(ProfileAction.OnConfirmRemoveBank) }, MoeDialogButtonStyle.DESTRUCTIVE),
            dismissButton = MoeDialogButton(stringResource(Res.string.cancel), { onAction(ProfileAction.OnDismissRemoveBank) }, MoeDialogButtonStyle.CANCEL),
            onDismissRequest = { onAction(ProfileAction.OnDismissRemoveBank) },
        )
    }

    if (state.isLogoutDialogVisible) {
        MoeAlertDialog(
            title = stringResource(Res.string.logout_dialog_title),
            message = stringResource(Res.string.logout_dialog_message),
            confirmButton = MoeDialogButton(stringResource(Res.string.logout), { onAction(ProfileAction.OnConfirmLogout) }, MoeDialogButtonStyle.DESTRUCTIVE),
            dismissButton = MoeDialogButton(stringResource(Res.string.cancel), { onAction(ProfileAction.OnDismissLogoutDialog) }, MoeDialogButtonStyle.CANCEL),
            onDismissRequest = { onAction(ProfileAction.OnDismissLogoutDialog) },
        )
    }
}

@Composable
private fun ProfileHeader(state: ProfileState, modifier: Modifier = Modifier) {
    val user = state.user
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
    ) {
        MoeAvatar(name = state.displayName, size = 88.dp, inverted = true)
        Spacer(Modifier.height(16.dp))
        Text(
            text = state.displayName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        val details = listOfNotNull(
            user?.username?.let { "@$it" },
            user?.phoneNumber?.takeIf { it.isNotBlank() }?.let(::formatPhoneForDisplay),
        )
        if (details.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = details.joinToString("  ·  "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
        user?.email?.takeIf { it.isNotBlank() }?.let { email ->
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SecuritySection(overview: WalletOverview, onAction: (ProfileAction) -> Unit, modifier: Modifier = Modifier) {
    val pin = overview.wallet.pin
    Column(modifier.padding(horizontal = 20.dp)) {
        MoeSectionHeader(title = stringResource(Res.string.security), modifier = Modifier.padding(top = 16.dp))
        MoeCard(Modifier.padding(top = 8.dp)) {
            MoeListRow(
                title = stringResource(Res.string.wallet_pin),
                subtitle = when {
                    !pin.isSet -> stringResource(Res.string.pin_not_set)
                    pin.isLocked -> stringResource(Res.string.pin_locked_until, pin.lockedUntil?.formatTime() ?: "")
                    else -> stringResource(Res.string.pin_on)
                },
                leading = { MoeIconTile(MoeIcon.Lock) },
                showChevron = true,
                onClick = { onAction(ProfileAction.OnPinClick) },
            )
            if (pin.isSet) {
                MoeRowDivider()
                MoeListRow(
                    title = stringResource(Res.string.reset_pin),
                    subtitle = stringResource(Res.string.reset_pin_subtitle),
                    leading = { MoeIconTile(MoeIcon.Info) },
                    showChevron = true,
                    onClick = { onAction(ProfileAction.OnResetPinClick) },
                )
            }
        }
    }
}

@Composable
private fun BanksSection(overview: WalletOverview, onAction: (ProfileAction) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = 20.dp)) {
        MoeSectionHeader(title = stringResource(Res.string.bank_accounts), modifier = Modifier.padding(top = 24.dp))
        MoeCard(Modifier.padding(top = 8.dp)) {
            overview.bankAccounts.forEach { bank ->
                MoeListRow(
                    title = "${bank.bankName} ••${bank.last4}",
                    subtitle = "${bank.holderName} · ${bank.sortCode}",
                    leading = { MoeIconTile(MoeIcon.Bank) },
                    trailing = { if (bank.isPrimary) MoeStatusPill(stringResource(Res.string.primary), MoePillTone.POSITIVE) },
                    onClick = { onAction(ProfileAction.OnBankClick(bank)) },
                )
                MoeRowDivider()
            }
            MoeListRow(
                title = stringResource(Res.string.link_bank_account),
                subtitle = stringResource(Res.string.link_bank_subtitle),
                leading = { MoeIconTile(MoeIcon.Add, inverted = true) },
                showChevron = true,
                onClick = { onAction(ProfileAction.OnLinkBankClick) },
            )
        }
    }
}

@Composable
private fun LimitsSection(overview: WalletOverview, modifier: Modifier = Modifier) {
    val limits = overview.limits
    val daily = limits.dailyOutgoing
    val fraction = if (daily.limit.pence > 0) (daily.used.pence.toFloat() / daily.limit.pence).coerceIn(0f, 1f) else 0f
    val animated by animateFloatAsState(fraction, tween(700), label = "dailyLimit")
    Column(modifier.padding(horizontal = 20.dp)) {
        MoeSectionHeader(title = stringResource(Res.string.limits), modifier = Modifier.padding(top = 24.dp))
        MoeCard(Modifier.padding(top = 8.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(Res.string.sent_today), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                    Text(
                        stringResource(Res.string.used_of_limit, daily.used.display, daily.limit.display),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.extended.textSecondary,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(6.dp).background(MaterialTheme.colorScheme.extended.border, CircleShape)) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(animated).background(MaterialTheme.colorScheme.onBackground, CircleShape))
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(Res.string.resets_at, daily.resetsAt.formatTime()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.extended.textTertiary,
                )
            }
            MoeRowDivider(startInset = 16.dp)
            LimitRow(stringResource(Res.string.per_payment), "${limits.minPerTransaction.display} – ${limits.maxPerTransaction.display}")
            MoeRowDivider(startInset = 16.dp)
            LimitRow(stringResource(Res.string.wallet_maximum), limits.maxBalance.display, stringResource(Res.string.can_add_more, limits.canAdd.display))
        }
    }
}

@Composable
private fun LimitRow(label: String, value: String, caption: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
            if (caption != null) Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.extended.textTertiary)
        }
    }
}
