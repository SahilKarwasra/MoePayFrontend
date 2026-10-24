package com.laarasoft.moepay.payments.presentation.requests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.core.designsystem.components.MoeAvatar
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeButtonStyle
import com.laarasoft.moepay.core.designsystem.components.MoeCard
import com.laarasoft.moepay.core.designsystem.components.MoeEmptyState
import com.laarasoft.moepay.core.designsystem.components.MoeRowDivider
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.motion.fadeUpOnAppear
import com.laarasoft.moepay.core.designsystem.native.MoeAlertDialog
import com.laarasoft.moepay.core.designsystem.native.MoeBarAction
import com.laarasoft.moepay.core.designsystem.native.MoeDialogButton
import com.laarasoft.moepay.core.designsystem.native.MoeDialogButtonStyle
import com.laarasoft.moepay.core.designsystem.native.MoeTopBar
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.domain.money.MoneyRequestAction
import com.laarasoft.moepay.core.domain.money.MoneyRequestDirection
import com.laarasoft.moepay.core.presentation.util.ObserveAsEvents
import com.laarasoft.moepay.core.presentation.util.formatDateTime
import com.laarasoft.moepay.core.ui.MoneyRequestStatusPill
import com.laarasoft.moepay.core.ui.MoneyResultContent
import com.laarasoft.moepay.core.ui.PinEntrySheet
import com.laarasoft.moepay.core.ui.PinLockedDialog
import com.laarasoft.moepay.core.ui.ResultDetail
import com.laarasoft.moepay.payments.presentation.resources.Res
import com.laarasoft.moepay.payments.presentation.resources.asked_you
import com.laarasoft.moepay.payments.presentation.resources.back
import com.laarasoft.moepay.payments.presentation.resources.cancel_message
import com.laarasoft.moepay.payments.presentation.resources.cancel_request
import com.laarasoft.moepay.payments.presentation.resources.cancel_title
import com.laarasoft.moepay.payments.presentation.resources.created
import com.laarasoft.moepay.payments.presentation.resources.decline
import com.laarasoft.moepay.payments.presentation.resources.decline_message
import com.laarasoft.moepay.payments.presentation.resources.decline_title
import com.laarasoft.moepay.payments.presentation.resources.expires
import com.laarasoft.moepay.payments.presentation.resources.keep
import com.laarasoft.moepay.payments.presentation.resources.new_balance
import com.laarasoft.moepay.payments.presentation.resources.note
import com.laarasoft.moepay.payments.presentation.resources.paid_done
import com.laarasoft.moepay.payments.presentation.resources.paid_message
import com.laarasoft.moepay.payments.presentation.resources.pay_button
import com.laarasoft.moepay.payments.presentation.resources.reference
import com.laarasoft.moepay.payments.presentation.resources.request_detail
import com.laarasoft.moepay.payments.presentation.resources.retry
import com.laarasoft.moepay.payments.presentation.resources.you_asked
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RequestDetailRoot(
    requestId: String,
    onBack: () -> Unit,
    onOpenResetPin: () -> Unit,
    viewModel: RequestDetailViewModel = koinViewModel(key = requestId) { parametersOf(requestId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            RequestDetailEvent.OpenResetPin -> onOpenResetPin()
        }
    }
    RequestDetailScreen(state = state, onAction = viewModel::onAction, onBack = onBack)
}

@Composable
fun RequestDetailScreen(
    state: RequestDetailState,
    onAction: (RequestDetailAction) -> Unit,
    onBack: () -> Unit,
) {
    state.paid?.let { paid ->
        MoneyResultContent(
            isSuccess = true,
            headline = stringResource(Res.string.paid_done, paid.movement.transaction.amount.display),
            message = stringResource(Res.string.paid_message, paid.request.counterparty.displayName),
            details = listOf(
                ResultDetail(stringResource(Res.string.new_balance), paid.movement.balance.display),
                ResultDetail(stringResource(Res.string.reference), paid.movement.transaction.reference),
            ),
            onDone = onBack,
        )
        return
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        MoeTopBar(title = stringResource(Res.string.request_detail), navigationAction = MoeBarAction(MoeIcon.Back, stringResource(Res.string.back), onBack))
        val request = state.request
        when {
            request == null && state.isLoading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground, strokeWidth = 2.dp)
            }
            request == null -> Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                MoeEmptyState(MoeIcon.Error, stringResource(Res.string.request_detail), state.error?.asString().orEmpty())
                MoeButton(stringResource(Res.string.retry), { onAction(RequestDetailAction.OnRetry) }, style = MoeButtonStyle.TEXT)
            }
            else -> {
                val name = request.counterparty.displayName
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
                ) {
                    Spacer(Modifier.height(24.dp))
                    MoeAvatar(name = name, size = 72.dp, inverted = true, modifier = Modifier.fadeUpOnAppear(0))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(if (request.direction == MoneyRequestDirection.INCOMING) Res.string.asked_you else Res.string.you_asked, name),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.extended.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                    Text(request.amount.display, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.fadeUpOnAppear(1))
                    Spacer(Modifier.height(10.dp))
                    MoneyRequestStatusPill(request.status)
                    Spacer(Modifier.height(28.dp))
                    MoeCard {
                        request.note?.let {
                            DetailRow(stringResource(Res.string.note), it)
                            MoeRowDivider(startInset = 16.dp)
                        }
                        DetailRow(stringResource(Res.string.created), request.createdAt.formatDateTime())
                        MoeRowDivider(startInset = 16.dp)
                        DetailRow(stringResource(Res.string.expires), request.expiresAt.formatDateTime())
                    }
                    state.actionError?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it.asString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                }
                Column(Modifier.windowInsetsPadding(WindowInsets.navigationBars).padding(horizontal = 24.dp, vertical = 12.dp)) {
                    if (MoneyRequestAction.PAY in request.actions) {
                        MoeButton(stringResource(Res.string.pay_button, request.amount.display), { onAction(RequestDetailAction.OnPayClick) }, enabled = !state.isActing, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                    }
                    if (MoneyRequestAction.DECLINE in request.actions) {
                        MoeButton(stringResource(Res.string.decline), { onAction(RequestDetailAction.OnDeclineClick) }, style = MoeButtonStyle.SECONDARY, isLoading = state.isActing, modifier = Modifier.fillMaxWidth())
                    }
                    if (MoneyRequestAction.CANCEL in request.actions) {
                        MoeButton(stringResource(Res.string.cancel_request), { onAction(RequestDetailAction.OnCancelClick) }, style = MoeButtonStyle.SECONDARY, isLoading = state.isActing, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }

    PinEntrySheet(
        visible = state.isPinVisible,
        subtitle = state.request?.let { stringResource(Res.string.pay_button, it.amount.display) },
        error = state.pinError,
        errorKey = state.pinErrorKey,
        isLoading = state.isPaying,
        onPinEntered = { onAction(RequestDetailAction.OnPinEntered(it)) },
        onForgotPin = { onAction(RequestDetailAction.OnForgotPin) },
        onDismiss = { onAction(RequestDetailAction.OnDismissPin) },
    )

    state.confirmation?.let { confirmation ->
        val name = state.request?.counterparty?.displayName.orEmpty()
        val isDecline = confirmation == RequestConfirmation.DECLINE
        MoeAlertDialog(
            title = stringResource(if (isDecline) Res.string.decline_title else Res.string.cancel_title),
            message = stringResource(if (isDecline) Res.string.decline_message else Res.string.cancel_message, name),
            confirmButton = MoeDialogButton(
                text = stringResource(if (isDecline) Res.string.decline else Res.string.cancel_request),
                onClick = { onAction(RequestDetailAction.OnConfirm) },
                style = MoeDialogButtonStyle.DESTRUCTIVE,
            ),
            dismissButton = MoeDialogButton(stringResource(Res.string.keep), { onAction(RequestDetailAction.OnDismissConfirmation) }, MoeDialogButtonStyle.CANCEL),
            onDismissRequest = { onAction(RequestDetailAction.OnDismissConfirmation) },
        )
    }

    state.lockedMessage?.let { message ->
        PinLockedDialog(message = message, onResetPin = { onAction(RequestDetailAction.OnForgotPin) }, onDismiss = { onAction(RequestDetailAction.OnDismissLocked) })
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.extended.textSecondary)
        Spacer(Modifier.padding(horizontal = 8.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.End)
    }
}
