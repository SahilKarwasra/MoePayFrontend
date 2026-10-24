package com.laarasoft.moepay.activity.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.activity.presentation.resources.Res
import com.laarasoft.moepay.activity.presentation.resources.back
import com.laarasoft.moepay.activity.presentation.resources.balance_after
import com.laarasoft.moepay.activity.presentation.resources.completed_at
import com.laarasoft.moepay.activity.presentation.resources.copied
import com.laarasoft.moepay.activity.presentation.resources.copy_reference
import com.laarasoft.moepay.activity.presentation.resources.date
import com.laarasoft.moepay.activity.presentation.resources.from
import com.laarasoft.moepay.activity.presentation.resources.note
import com.laarasoft.moepay.activity.presentation.resources.reason
import com.laarasoft.moepay.activity.presentation.resources.reference
import com.laarasoft.moepay.activity.presentation.resources.retry
import com.laarasoft.moepay.activity.presentation.resources.status_completed
import com.laarasoft.moepay.activity.presentation.resources.status_failed
import com.laarasoft.moepay.activity.presentation.resources.to
import com.laarasoft.moepay.activity.presentation.resources.transaction
import com.laarasoft.moepay.activity.presentation.resources.type
import com.laarasoft.moepay.activity.presentation.resources.type_add
import com.laarasoft.moepay.activity.presentation.resources.type_p2p
import com.laarasoft.moepay.activity.presentation.resources.type_request
import com.laarasoft.moepay.activity.presentation.resources.type_withdraw
import com.laarasoft.moepay.activity.presentation.resources.view_request
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeButtonStyle
import com.laarasoft.moepay.core.designsystem.components.MoeCard
import com.laarasoft.moepay.core.designsystem.components.MoeEmptyState
import com.laarasoft.moepay.core.designsystem.components.MoePillTone
import com.laarasoft.moepay.core.designsystem.components.MoeRowDivider
import com.laarasoft.moepay.core.designsystem.components.MoeStatusPill
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.motion.fadeUpOnAppear
import com.laarasoft.moepay.core.designsystem.native.MoeBarAction
import com.laarasoft.moepay.core.designsystem.native.MoeTopBar
import com.laarasoft.moepay.core.designsystem.native.rememberClipboard
import com.laarasoft.moepay.core.designsystem.native.rememberHaptics
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.domain.money.PartyKind
import com.laarasoft.moepay.core.domain.money.Transaction
import com.laarasoft.moepay.core.domain.money.TransactionDirection
import com.laarasoft.moepay.core.domain.money.TransactionType
import com.laarasoft.moepay.core.presentation.util.formatDateTime
import com.laarasoft.moepay.core.ui.TransactionLeading
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TransactionDetailRoot(
    transactionId: String,
    onBack: () -> Unit,
    onOpenRequest: (String) -> Unit,
    viewModel: TransactionDetailViewModel = koinViewModel(key = transactionId) { parametersOf(transactionId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TransactionDetailScreen(state = state, onAction = viewModel::onAction, onBack = onBack, onOpenRequest = onOpenRequest)
}

@Composable
fun TransactionDetailScreen(
    state: TransactionDetailState,
    onAction: (TransactionDetailAction) -> Unit,
    onBack: () -> Unit,
    onOpenRequest: (String) -> Unit,
) {
    val copy = rememberClipboard()
    val haptics = rememberHaptics()
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        MoeTopBar(title = stringResource(Res.string.transaction), navigationAction = MoeBarAction(MoeIcon.Back, stringResource(Res.string.back), onBack))
        val transaction = state.transaction
        when {
            transaction == null && state.isLoading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground, strokeWidth = 2.dp)
            }
            transaction == null -> Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                MoeEmptyState(MoeIcon.Error, stringResource(Res.string.transaction), state.error?.asString().orEmpty())
                MoeButton(stringResource(Res.string.retry), { onAction(TransactionDetailAction.OnRetry) }, style = MoeButtonStyle.TEXT)
            }
            else -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            ) {
                Spacer(Modifier.height(24.dp))
                Box(Modifier.fadeUpOnAppear(0)) { TransactionLeading(transaction) }
                Spacer(Modifier.height(14.dp))
                Text(
                    transaction.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.extended.textSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    transaction.signedDisplay,
                    style = MaterialTheme.typography.displayMedium,
                    color = if (transaction.isFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                    textDecoration = if (transaction.isFailed) TextDecoration.LineThrough else null,
                    modifier = Modifier.fadeUpOnAppear(1),
                )
                Spacer(Modifier.height(10.dp))
                MoeStatusPill(
                    text = stringResource(if (transaction.isFailed) Res.string.status_failed else Res.string.status_completed),
                    tone = if (transaction.isFailed) MoePillTone.NEGATIVE else MoePillTone.POSITIVE,
                )
                Spacer(Modifier.height(28.dp))
                DetailsCard(transaction)
                Spacer(Modifier.height(16.dp))
                MoeButton(
                    text = stringResource(if (state.isReferenceCopied) Res.string.copied else Res.string.copy_reference),
                    onClick = {
                        copy(transaction.reference)
                        haptics.success()
                        onAction(TransactionDetailAction.OnReferenceCopied)
                    },
                    style = MoeButtonStyle.SECONDARY,
                    modifier = Modifier.fillMaxWidth(),
                )
                transaction.moneyRequestId?.let { requestId ->
                    Spacer(Modifier.height(8.dp))
                    MoeButton(stringResource(Res.string.view_request), { onOpenRequest(requestId) }, style = MoeButtonStyle.TEXT, modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DetailsCard(transaction: Transaction) {
    val party = transaction.counterparty
    val partyLabel = when (party.kind) {
        PartyKind.BANK -> listOfNotNull(party.bankName ?: party.displayName, party.accountLast4?.let { "••$it" }).joinToString(" ")
        PartyKind.USER -> party.displayName + (party.username?.let { " · @$it" } ?: "")
    }
    MoeCard {
        Row1(stringResource(if (transaction.direction == TransactionDirection.OUT) Res.string.to else Res.string.from), partyLabel)
        MoeRowDivider(startInset = 16.dp)
        Row1(
            stringResource(Res.string.type),
            stringResource(
                when (transaction.type) {
                    TransactionType.ADD_MONEY -> Res.string.type_add
                    TransactionType.WITHDRAW -> Res.string.type_withdraw
                    TransactionType.REQUEST_PAYMENT -> Res.string.type_request
                    else -> Res.string.type_p2p
                }
            ),
        )
        transaction.note?.let {
            MoeRowDivider(startInset = 16.dp)
            Row1(stringResource(Res.string.note), it)
        }
        transaction.failure?.let {
            MoeRowDivider(startInset = 16.dp)
            Row1(stringResource(Res.string.reason), it.reason)
        }
        transaction.balanceAfter?.let {
            MoeRowDivider(startInset = 16.dp)
            Row1(stringResource(Res.string.balance_after), it.display)
        }
        MoeRowDivider(startInset = 16.dp)
        Row1(stringResource(Res.string.date), transaction.createdAt.formatDateTime())
        transaction.completedAt?.takeIf { it != transaction.createdAt }?.let {
            MoeRowDivider(startInset = 16.dp)
            Row1(stringResource(Res.string.completed_at), it.formatDateTime())
        }
        MoeRowDivider(startInset = 16.dp)
        Row1(stringResource(Res.string.reference), transaction.reference)
    }
}

@Composable
private fun Row1(label: String, value: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.extended.textSecondary)
        Spacer(Modifier.width(16.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}
