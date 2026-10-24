package com.laarasoft.moepay.wallet.presentation.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeEmptyState
import com.laarasoft.moepay.core.designsystem.components.MoeIconTile
import com.laarasoft.moepay.core.designsystem.components.MoeListRow
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.icon.painter
import com.laarasoft.moepay.core.designsystem.native.MoeBarAction
import com.laarasoft.moepay.core.designsystem.native.MoeSheet
import com.laarasoft.moepay.core.designsystem.native.MoeTopBar
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.domain.wallet.BankAccount
import com.laarasoft.moepay.core.presentation.money.formatPence
import com.laarasoft.moepay.core.ui.AmountEntryContent
import com.laarasoft.moepay.core.ui.MoneyResultContent
import com.laarasoft.moepay.core.ui.PinEntrySheet
import com.laarasoft.moepay.core.ui.PinLockedDialog
import com.laarasoft.moepay.core.ui.ResultDetail
import com.laarasoft.moepay.wallet.domain.BankTransferDirection
import com.laarasoft.moepay.wallet.presentation.resources.Res
import com.laarasoft.moepay.wallet.presentation.resources.add_done
import com.laarasoft.moepay.wallet.presentation.resources.add_failed
import com.laarasoft.moepay.wallet.presentation.resources.add_money
import com.laarasoft.moepay.wallet.presentation.resources.arrives
import com.laarasoft.moepay.wallet.presentation.resources.back
import com.laarasoft.moepay.wallet.presentation.resources.change
import com.laarasoft.moepay.wallet.presentation.resources.choose_account
import com.laarasoft.moepay.wallet.presentation.resources.confirm
import com.laarasoft.moepay.wallet.presentation.resources.from
import com.laarasoft.moepay.wallet.presentation.resources.from_bank
import com.laarasoft.moepay.wallet.presentation.resources.instantly
import com.laarasoft.moepay.wallet.presentation.resources.link_bank_account
import com.laarasoft.moepay.wallet.presentation.resources.new_balance
import com.laarasoft.moepay.wallet.presentation.resources.no_bank_message
import com.laarasoft.moepay.wallet.presentation.resources.no_bank_title
import com.laarasoft.moepay.wallet.presentation.resources.reference
import com.laarasoft.moepay.wallet.presentation.resources.review
import com.laarasoft.moepay.wallet.presentation.resources.review_add_title
import com.laarasoft.moepay.wallet.presentation.resources.review_withdraw_title
import com.laarasoft.moepay.wallet.presentation.resources.to
import com.laarasoft.moepay.wallet.presentation.resources.to_bank
import com.laarasoft.moepay.wallet.presentation.resources.withdraw
import com.laarasoft.moepay.wallet.presentation.resources.withdraw_done
import com.laarasoft.moepay.wallet.presentation.resources.withdraw_failed
import com.laarasoft.moepay.wallet.presentation.resources.your_wallet
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BankTransferRoot(
    direction: BankTransferDirection,
    onBack: () -> Unit,
    onFinished: () -> Unit,
    onLinkBank: () -> Unit,
    onResetPin: () -> Unit,
    viewModel: BankTransferViewModel = koinViewModel(key = direction.name) { parametersOf(direction) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BankTransferScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
        onFinished = onFinished,
        onLinkBank = onLinkBank,
        onResetPin = {
            viewModel.onAction(BankTransferAction.OnDismissLocked)
            onResetPin()
        },
    )
}

private fun BankAccount.label() = "$bankName ••$last4"

@Composable
fun BankTransferScreen(
    state: BankTransferState,
    onAction: (BankTransferAction) -> Unit,
    onBack: () -> Unit,
    onFinished: () -> Unit,
    onLinkBank: () -> Unit,
    onResetPin: () -> Unit,
) {
    val isAdd = state.direction == BankTransferDirection.ADD_MONEY

    when (val outcome = state.outcome) {
        is BankTransferOutcome.Succeeded -> {
            MoneyResultContent(
                isSuccess = true,
                headline = stringResource(if (isAdd) Res.string.add_done else Res.string.withdraw_done, outcome.movement.transaction.amount.display),
                message = stringResource(if (isAdd) Res.string.from_bank else Res.string.to_bank, outcome.bank.label()),
                details = listOf(
                    ResultDetail(stringResource(Res.string.new_balance), outcome.movement.balance.display),
                    ResultDetail(stringResource(Res.string.reference), outcome.movement.transaction.reference),
                ),
                onDone = onFinished,
            )
            return
        }
        is BankTransferOutcome.Declined -> {
            MoneyResultContent(
                isSuccess = false,
                headline = stringResource(if (isAdd) Res.string.add_failed else Res.string.withdraw_failed),
                message = outcome.reason.asString(),
                details = emptyList(),
                onDone = onFinished,
                onTryAgain = { onAction(BankTransferAction.OnTryAgain) },
            )
            return
        }
        null -> Unit
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        MoeTopBar(
            title = stringResource(if (isAdd) Res.string.add_money else Res.string.withdraw),
            navigationAction = MoeBarAction(MoeIcon.Back, stringResource(Res.string.back), onBack),
        )
        val overview = state.overview
        if (overview != null && overview.bankAccounts.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MoeEmptyState(MoeIcon.Bank, stringResource(Res.string.no_bank_title), stringResource(Res.string.no_bank_message))
                    Spacer(Modifier.height(20.dp))
                    MoeButton(stringResource(Res.string.link_bank_account), onLinkBank)
                }
            }
            return@Column
        }
        AmountEntryContent(
            amount = state.amount,
            onKey = { onAction(BankTransferAction.OnKey(it)) },
            rejectKey = state.rejectKey,
            helper = state.helper,
            isHelperError = state.isHelperError,
            continueLabel = stringResource(Res.string.review),
            canContinue = state.canContinue,
            onContinue = { onAction(BankTransferAction.OnContinue) },
            modifier = Modifier.weight(1f),
            header = {
                state.selectedBank?.let { bank ->
                    BankChip(
                        label = stringResource(if (isAdd) Res.string.from_bank else Res.string.to_bank, bank.label()),
                        actionLabel = stringResource(Res.string.change).takeIf { (overview?.bankAccounts?.size ?: 0) > 1 },
                        onClick = { onAction(BankTransferAction.OnChangeBankClick) },
                    )
                }
            },
        )
    }

    BankPickerSheet(
        visible = state.isBankPickerVisible,
        banks = state.overview?.bankAccounts.orEmpty(),
        selectedId = state.selectedBank?.id,
        onSelect = { onAction(BankTransferAction.OnBankSelected(it)) },
        onDismiss = { onAction(BankTransferAction.OnDismissBankPicker) },
    )

    MoeSheet(visible = state.isReviewVisible, onDismissRequest = { onAction(BankTransferAction.OnDismissReview) }) {
        val bank = state.selectedBank
        val amountText = formatPence(state.amount.pence)
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Text(
                stringResource(if (isAdd) Res.string.review_add_title else Res.string.review_withdraw_title, amountText),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(20.dp))
            ReviewRow(stringResource(Res.string.from), if (isAdd) bank?.label().orEmpty() else stringResource(Res.string.your_wallet))
            ReviewRow(stringResource(Res.string.to), if (isAdd) stringResource(Res.string.your_wallet) else bank?.label().orEmpty())
            ReviewRow(stringResource(Res.string.arrives), stringResource(Res.string.instantly))
            Spacer(Modifier.height(24.dp))
            MoeButton(stringResource(Res.string.confirm), { onAction(BankTransferAction.OnConfirm) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
        }
    }

    PinEntrySheet(
        visible = state.isPinVisible,
        subtitle = stringResource(if (isAdd) Res.string.review_add_title else Res.string.review_withdraw_title, formatPence(state.amount.pence)),
        error = state.pinError,
        errorKey = state.pinErrorKey,
        isLoading = state.isSubmitting,
        onPinEntered = { onAction(BankTransferAction.OnPinEntered(it)) },
        onForgotPin = {
            onAction(BankTransferAction.OnDismissPin)
            onResetPin()
        },
        onDismiss = { onAction(BankTransferAction.OnDismissPin) },
    )

    state.lockedMessage?.let { message ->
        PinLockedDialog(message = message, onResetPin = onResetPin, onDismiss = { onAction(BankTransferAction.OnDismissLocked) })
    }
}

@Composable
private fun BankChip(label: String, actionLabel: String?, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(top = 12.dp)
            .border(1.dp, MaterialTheme.colorScheme.extended.border, CircleShape)
            .clickable(enabled = actionLabel != null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Icon(MoeIcon.Bank.painter(), null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
        if (actionLabel != null) {
            Spacer(Modifier.width(8.dp))
            Text(actionLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.extended.textSecondary)
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.extended.textSecondary)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun BankPickerSheet(
    visible: Boolean,
    banks: List<BankAccount>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    MoeSheet(visible = visible, onDismissRequest = onDismiss) {
        Text(
            stringResource(Res.string.choose_account),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        banks.forEach { bank ->
            MoeListRow(
                title = bank.label(),
                subtitle = "${bank.holderName} · ${bank.sortCode}",
                leading = { MoeIconTile(MoeIcon.Bank) },
                trailing = {
                    if (bank.id == selectedId) Icon(MoeIcon.Check.painter(), null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
                },
                onClick = { onSelect(bank.id) },
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}
