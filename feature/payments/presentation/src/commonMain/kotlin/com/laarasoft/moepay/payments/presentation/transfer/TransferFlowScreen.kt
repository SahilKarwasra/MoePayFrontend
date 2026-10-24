package com.laarasoft.moepay.payments.presentation.transfer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.core.designsystem.components.MoeAvatar
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeCard
import com.laarasoft.moepay.core.designsystem.components.MoeListRow
import com.laarasoft.moepay.core.designsystem.components.MoeSectionHeader
import com.laarasoft.moepay.core.designsystem.components.MoeSkeletonRows
import com.laarasoft.moepay.core.designsystem.components.MoeTextField
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.icon.painter
import com.laarasoft.moepay.core.designsystem.native.MoeBarAction
import com.laarasoft.moepay.core.designsystem.native.MoeSheet
import com.laarasoft.moepay.core.designsystem.native.MoeTopBar
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.domain.money.Recipient
import com.laarasoft.moepay.core.presentation.money.formatPence
import com.laarasoft.moepay.core.presentation.util.ObserveAsEvents
import com.laarasoft.moepay.core.presentation.util.UiText
import com.laarasoft.moepay.core.presentation.util.formatDateTime
import com.laarasoft.moepay.core.ui.AmountEntryContent
import com.laarasoft.moepay.core.ui.MoneyResultContent
import com.laarasoft.moepay.core.ui.PinEntrySheet
import com.laarasoft.moepay.core.ui.PinLockedDialog
import com.laarasoft.moepay.core.ui.ResultDetail
import com.laarasoft.moepay.payments.presentation.resources.Res
import com.laarasoft.moepay.payments.presentation.resources.amount
import com.laarasoft.moepay.payments.presentation.resources.back
import com.laarasoft.moepay.payments.presentation.resources.balance_helper
import com.laarasoft.moepay.payments.presentation.resources.below_minimum
import com.laarasoft.moepay.payments.presentation.resources.expires
import com.laarasoft.moepay.payments.presentation.resources.from_person
import com.laarasoft.moepay.payments.presentation.resources.new_balance
import com.laarasoft.moepay.payments.presentation.resources.note_placeholder
import com.laarasoft.moepay.payments.presentation.resources.over_balance
import com.laarasoft.moepay.payments.presentation.resources.over_limit
import com.laarasoft.moepay.payments.presentation.resources.paying_from_qr
import com.laarasoft.moepay.payments.presentation.resources.recent
import com.laarasoft.moepay.payments.presentation.resources.reference
import com.laarasoft.moepay.payments.presentation.resources.request_button
import com.laarasoft.moepay.payments.presentation.resources.request_from
import com.laarasoft.moepay.payments.presentation.resources.request_money
import com.laarasoft.moepay.payments.presentation.resources.requested_done
import com.laarasoft.moepay.payments.presentation.resources.requested_message
import com.laarasoft.moepay.payments.presentation.resources.review
import com.laarasoft.moepay.payments.presentation.resources.scan
import com.laarasoft.moepay.payments.presentation.resources.search_hint
import com.laarasoft.moepay.payments.presentation.resources.search_placeholder
import com.laarasoft.moepay.payments.presentation.resources.send_button
import com.laarasoft.moepay.payments.presentation.resources.send_money
import com.laarasoft.moepay.payments.presentation.resources.send_to
import com.laarasoft.moepay.payments.presentation.resources.sent_done
import com.laarasoft.moepay.payments.presentation.resources.to_person
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val NOTE_MAX = 140

@Composable
fun TransferFlowRoot(
    args: TransferFlowArgs,
    onClose: () -> Unit,
    onOpenScanner: () -> Unit,
    onOpenResetPin: () -> Unit,
    viewModel: TransferFlowViewModel = koinViewModel { parametersOf(args) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            TransferFlowEvent.Close -> onClose()
            TransferFlowEvent.OpenScanner -> onOpenScanner()
            TransferFlowEvent.OpenResetPin -> onOpenResetPin()
        }
    }
    TransferFlowScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun TransferFlowScreen(
    state: TransferFlowState,
    onAction: (TransferFlowAction) -> Unit,
) {
    val isSend = state.mode == TransferMode.SEND
    when (val result = state.result) {
        is TransferResult.Sent -> {
            MoneyResultContent(
                isSuccess = true,
                headline = stringResource(Res.string.sent_done, result.movement.transaction.amount.display),
                message = stringResource(Res.string.to_person, result.recipient.displayName),
                details = listOf(
                    ResultDetail(stringResource(Res.string.new_balance), result.movement.balance.display),
                    ResultDetail(stringResource(Res.string.reference), result.movement.transaction.reference),
                ),
                onDone = { onAction(TransferFlowAction.OnDone) },
            )
            return
        }
        is TransferResult.Requested -> {
            MoneyResultContent(
                isSuccess = true,
                headline = stringResource(Res.string.requested_done),
                message = stringResource(Res.string.requested_message, result.request.counterparty.displayName, result.request.amount.display),
                details = listOf(
                    ResultDetail(stringResource(Res.string.amount), result.request.amount.display),
                    ResultDetail(stringResource(Res.string.expires), result.request.expiresAt.formatDateTime()),
                ),
                onDone = { onAction(TransferFlowAction.OnDone) },
            )
            return
        }
        null -> Unit
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        MoeTopBar(
            title = stringResource(if (isSend) Res.string.send_money else Res.string.request_money),
            navigationAction = MoeBarAction(MoeIcon.Back, stringResource(Res.string.back)) { onAction(TransferFlowAction.OnBack) },
            actions = if (isSend && state.step == TransferStep.RECIPIENT) {
                listOf(MoeBarAction(MoeIcon.Scan, stringResource(Res.string.scan)) { onAction(TransferFlowAction.OnScanClick) })
            } else emptyList(),
        )
        if (state.isLoadingQr) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground, strokeWidth = 2.dp)
            }
            return@Column
        }
        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                (slideInHorizontally(tween(260)) { if (forward) it / 3 else -it / 3 } + fadeIn(tween(260))) togetherWith
                    (slideOutHorizontally(tween(220)) { if (forward) -it / 3 else it / 3 } + fadeOut(tween(220)))
            },
            label = "transferStep",
            modifier = Modifier.weight(1f),
        ) { step ->
            when (step) {
                TransferStep.RECIPIENT -> RecipientStep(state, onAction)
                TransferStep.AMOUNT -> AmountStep(state, onAction)
            }
        }
    }

    ReviewSheet(state, onAction)

    PinEntrySheet(
        visible = state.isPinVisible,
        subtitle = state.recipient?.let { stringResource(Res.string.send_to, formatPence(state.amount.pence)) + " " + stringResource(Res.string.to_person, it.displayName) },
        error = state.pinError,
        errorKey = state.pinErrorKey,
        isLoading = state.isSubmitting,
        onPinEntered = { onAction(TransferFlowAction.OnPinEntered(it)) },
        onForgotPin = { onAction(TransferFlowAction.OnForgotPin) },
        onDismiss = { onAction(TransferFlowAction.OnDismissPin) },
    )

    state.lockedMessage?.let { message ->
        PinLockedDialog(
            message = message,
            onResetPin = { onAction(TransferFlowAction.OnForgotPin) },
            onDismiss = { onAction(TransferFlowAction.OnDismissLocked) },
        )
    }
}

@Composable
private fun RecipientStep(state: TransferFlowState, onAction: (TransferFlowAction) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        item {
            Spacer(Modifier.height(8.dp))
            MoeTextField(
                state = state.queryState,
                placeholder = stringResource(Res.string.search_placeholder),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Search,
                ),
                leadingContent = {
                    Icon(MoeIcon.Search.painter(), null, tint = MaterialTheme.colorScheme.extended.textTertiary, modifier = Modifier.size(20.dp))
                },
                trailingContent = if (state.isResolving) {
                    { CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onBackground) }
                } else null,
                supportingText = state.searchError?.asString() ?: stringResource(Res.string.search_hint),
                isError = state.searchError != null,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            )
            Spacer(Modifier.height(16.dp))
        }
        state.searchResult?.let { recipient ->
            item(key = "result") {
                MoeCard { RecipientRow(recipient) { onAction(TransferFlowAction.OnRecipientSelected(recipient)) } }
                Spacer(Modifier.height(16.dp))
            }
        }
        if (state.queryState.text.isEmpty() && state.recents.isNotEmpty()) {
            item { MoeSectionHeader(stringResource(Res.string.recent)) }
            item {
                MoeCard(Modifier.padding(top = 8.dp)) {
                    state.recents.forEach { recent ->
                        RecipientRow(recent.recipient) { onAction(TransferFlowAction.OnRecipientSelected(recent.recipient)) }
                    }
                }
            }
        } else if (state.isResolving && state.searchResult == null) {
            item { MoeCard { MoeSkeletonRows(count = 1) } }
        }
    }
}

@Composable
private fun RecipientRow(recipient: Recipient, onClick: () -> Unit) {
    MoeListRow(
        title = recipient.displayName,
        subtitle = listOfNotNull(recipient.username?.let { "@$it" }, recipient.phoneMasked.ifBlank { null }).joinToString(" · "),
        leading = { MoeAvatar(name = recipient.displayName) },
        showChevron = true,
        onClick = onClick,
    )
}

@Composable
private fun AmountStep(state: TransferFlowState, onAction: (TransferFlowAction) -> Unit) {
    val recipient = state.recipient ?: return
    val isSend = state.mode == TransferMode.SEND
    val overview = state.overview
    val pence = state.amount.pence
    val (helper, isError) = when {
        state.submitError != null -> state.submitError to true
        state.amountLocked -> UiText.Resource(Res.string.paying_from_qr, arrayOf(recipient.displayName)) to false
        pence in 1 until state.minPence() -> UiText.Resource(Res.string.below_minimum, arrayOf(overview?.limits?.minPerTransaction?.display ?: "")) to true
        isSend && overview != null && pence > overview.wallet.balance.pence -> UiText.Resource(Res.string.over_balance) to true
        pence > state.maxPence() -> UiText.Resource(Res.string.over_limit) to true
        isSend && overview != null -> UiText.Resource(
            Res.string.balance_helper,
            arrayOf(overview.wallet.balance.display, overview.limits.dailyOutgoing.remaining.display),
        ) to false
        else -> null to false
    }
    AmountEntryContent(
        amount = state.amount,
        onKey = { onAction(TransferFlowAction.OnKey(it)) },
        rejectKey = state.rejectKey,
        helper = helper,
        isHelperError = isError,
        continueLabel = stringResource(Res.string.review),
        canContinue = state.recipient != null && pence >= state.minPence() && pence <= state.maxPence(),
        onContinue = { onAction(TransferFlowAction.OnContinue) },
        amountLocked = state.amountLocked,
        header = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 16.dp)) {
                MoeAvatar(name = recipient.displayName, size = 56.dp, inverted = true)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(if (isSend) Res.string.to_person else Res.string.from_person, recipient.displayName),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                recipient.username?.let {
                    Text("@$it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.extended.textSecondary)
                }
            }
        },
    )
}

@Composable
private fun ReviewSheet(state: TransferFlowState, onAction: (TransferFlowAction) -> Unit) {
    val isSend = state.mode == TransferMode.SEND
    val amountText = formatPence(state.amount.pence)
    MoeSheet(visible = state.isReviewVisible, onDismissRequest = { onAction(TransferFlowAction.OnDismissReview) }) {
        val recipient = state.recipient ?: return@MoeSheet
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            MoeAvatar(name = recipient.displayName, size = 64.dp, inverted = true)
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(if (isSend) Res.string.send_to else Res.string.request_from, amountText),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                stringResource(if (isSend) Res.string.to_person else Res.string.from_person, recipient.displayName) +
                    (recipient.username?.let { " · @$it" } ?: ""),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            MoeTextField(
                state = state.noteState,
                placeholder = stringResource(Res.string.note_placeholder),
                inputTransformation = InputTransformation.maxLength(NOTE_MAX),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                enabled = !state.amountLocked || state.noteState.text.isEmpty(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
            MoeButton(
                text = stringResource(if (isSend) Res.string.send_button else Res.string.request_button, amountText),
                onClick = { onAction(TransferFlowAction.OnConfirm) },
                isLoading = state.isSubmitting && !isSend,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}
