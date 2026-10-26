package com.laarasoft.moepay.chat.presentation.money

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.chat.presentation.resources.Res
import com.laarasoft.moepay.chat.presentation.resources.add_note
import com.laarasoft.moepay.chat.presentation.resources.continue_label
import com.laarasoft.moepay.chat.presentation.resources.max_amount
import com.laarasoft.moepay.chat.presentation.resources.pin_subtitle_send
import com.laarasoft.moepay.chat.presentation.resources.request_money_from
import com.laarasoft.moepay.chat.presentation.resources.review_request
import com.laarasoft.moepay.chat.presentation.resources.review_send
import com.laarasoft.moepay.chat.presentation.resources.send_money_to
import com.laarasoft.moepay.core.designsystem.components.MoeAvatar
import com.laarasoft.moepay.core.designsystem.native.MoeAlertDialog
import com.laarasoft.moepay.core.designsystem.native.MoeDialogButton
import com.laarasoft.moepay.core.designsystem.native.MoeSheet
import com.laarasoft.moepay.core.designsystem.native.MoeSheetSize
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.presentation.money.formatPence
import com.laarasoft.moepay.core.presentation.util.UiText
import com.laarasoft.moepay.core.ui.AmountEntryContent
import com.laarasoft.moepay.core.ui.PinEntrySheet
import com.laarasoft.moepay.core.ui.PinLockedDialog
import org.jetbrains.compose.resources.stringResource

private const val MAX_NOTE = 140

/** Amount sheet → (send) native PIN sheet. Everything here is a native sheet or alert. */
@Composable
fun ChatMoneySheets(
    state: ChatMoneyState,
    onAction: (ChatMoneyAction) -> Unit,
) {
    val mode = state.mode
    MoeSheet(
        visible = mode != null,
        onDismissRequest = { onAction(ChatMoneyAction.OnDismiss) },
        size = MoeSheetSize.LARGE,
    ) {
        val isSend = mode == ChatMoneyMode.SEND
        val amountText = state.amount.display
        AmountEntryContent(
            amount = state.amount,
            onKey = { onAction(ChatMoneyAction.OnKey(it)) },
            rejectKey = state.rejectKey,
            helper = state.maxPence.takeIf { it != Long.MAX_VALUE && it > 0 }?.let { UiText.DynamicString(stringResource(Res.string.max_amount, formatPence(it))) },
            isHelperError = false,
            continueLabel = if (state.amount.isEmpty) {
                stringResource(Res.string.continue_label)
            } else {
                stringResource(if (isSend) Res.string.review_send else Res.string.review_request, amountText)
            },
            canContinue = state.canContinue,
            onContinue = { onAction(ChatMoneyAction.OnContinue) },
            modifier = Modifier.fillMaxSize(),
            header = {
                MoeAvatar(name = state.peerName, size = 56.dp, inverted = true, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(if (isSend) Res.string.send_money_to else Res.string.request_money_from, state.peerName),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            },
            belowAmount = { NoteField(state) },
        )
    }

    PinEntrySheet(
        visible = state.isPinVisible,
        subtitle = stringResource(Res.string.pin_subtitle_send, state.amount.display, state.peerName),
        error = state.pinError,
        errorKey = state.pinErrorKey,
        isLoading = state.isSubmitting,
        onPinEntered = { onAction(ChatMoneyAction.OnPinEntered(it)) },
        onForgotPin = { onAction(ChatMoneyAction.OnForgotPin) },
        onDismiss = { onAction(ChatMoneyAction.OnDismissPin) },
    )

    state.lockedMessage?.let { message ->
        PinLockedDialog(
            message = message,
            onResetPin = {
                onAction(ChatMoneyAction.OnDismissLocked)
                onAction(ChatMoneyAction.OnForgotPin)
            },
            onDismiss = { onAction(ChatMoneyAction.OnDismissLocked) },
        )
    }

    state.error?.let { error ->
        MoeAlertDialog(
            title = error.asString(),
            message = null,
            confirmButton = MoeDialogButton("OK", { onAction(ChatMoneyAction.OnDismissError) }),
            onDismissRequest = { onAction(ChatMoneyAction.OnDismissError) },
        )
    }
}

@Composable
private fun NoteField(state: ChatMoneyState) {
    val colors = MaterialTheme.colorScheme
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .widthIn(min = 180.dp, max = 280.dp)
            .background(colors.extended.fieldBackground, CircleShape)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        if (state.noteState.text.isEmpty()) {
            Text(stringResource(Res.string.add_note), style = MaterialTheme.typography.bodyMedium, color = colors.extended.textTertiary)
        }
        BasicTextField(
            state = state.noteState,
            lineLimits = TextFieldLineLimits.SingleLine,
            inputTransformation = InputTransformation.maxLength(MAX_NOTE),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface, textAlign = TextAlign.Center),
            cursorBrush = SolidColor(colors.onSurface),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
