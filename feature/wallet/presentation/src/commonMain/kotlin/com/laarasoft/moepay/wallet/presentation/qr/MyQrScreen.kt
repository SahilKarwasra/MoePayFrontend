package com.laarasoft.moepay.wallet.presentation.qr

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeButtonStyle
import com.laarasoft.moepay.core.designsystem.components.MoeQrCode
import com.laarasoft.moepay.core.designsystem.components.MoeTextField
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.motion.fadeUpOnAppear
import com.laarasoft.moepay.core.designsystem.native.MoeBarAction
import com.laarasoft.moepay.core.designsystem.native.MoeSheet
import com.laarasoft.moepay.core.designsystem.native.MoeSheetSize
import com.laarasoft.moepay.core.designsystem.native.MoeTopBar
import com.laarasoft.moepay.core.designsystem.native.rememberClipboard
import com.laarasoft.moepay.core.designsystem.native.rememberHaptics
import com.laarasoft.moepay.core.designsystem.native.rememberShareSheet
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.ui.AmountEntryContent
import com.laarasoft.moepay.wallet.presentation.resources.Res
import com.laarasoft.moepay.wallet.presentation.resources.back
import com.laarasoft.moepay.wallet.presentation.resources.my_qr
import com.laarasoft.moepay.wallet.presentation.resources.qr_add_amount
import com.laarasoft.moepay.wallet.presentation.resources.qr_any_amount
import com.laarasoft.moepay.wallet.presentation.resources.qr_clear_amount
import com.laarasoft.moepay.wallet.presentation.resources.qr_copied
import com.laarasoft.moepay.wallet.presentation.resources.qr_copy
import com.laarasoft.moepay.wallet.presentation.resources.qr_description
import com.laarasoft.moepay.wallet.presentation.resources.qr_edit_amount
import com.laarasoft.moepay.wallet.presentation.resources.qr_note_placeholder
import com.laarasoft.moepay.wallet.presentation.resources.qr_set_amount
import com.laarasoft.moepay.wallet.presentation.resources.qr_share
import com.laarasoft.moepay.wallet.presentation.resources.qr_share_text
import com.laarasoft.moepay.wallet.presentation.resources.qr_title
import com.laarasoft.moepay.wallet.presentation.resources.retry
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val NOTE_MAX = 140

@Composable
fun MyQrRoot(
    onBack: () -> Unit,
    viewModel: MyQrViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MyQrScreen(state = state, onAction = viewModel::onAction, onBack = onBack)
}

@Composable
fun MyQrScreen(
    state: MyQrState,
    onAction: (MyQrAction) -> Unit,
    onBack: () -> Unit,
) {
    val share = rememberShareSheet()
    val copy = rememberClipboard()
    val haptics = rememberHaptics()
    val qr = state.qr
    val shareText = qr?.let { stringResource(Res.string.qr_share_text, it.payload) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        MoeTopBar(
            title = stringResource(Res.string.my_qr),
            navigationAction = MoeBarAction(MoeIcon.Back, stringResource(Res.string.back), onBack),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            Text(stringResource(Res.string.qr_title), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(20.dp))
            Box(Modifier.size(272.dp), contentAlignment = Alignment.Center) {
                when {
                    qr != null -> AnimatedContent(
                        targetState = qr.payload,
                        transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.96f)) togetherWith fadeOut() },
                        label = "qr",
                    ) { payload ->
                        MoeQrCode(data = payload, size = 240.dp, contentDescription = stringResource(Res.string.qr_description))
                    }
                    state.error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error.asString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        MoeButton(stringResource(Res.string.retry), { onAction(MyQrAction.OnRetry) }, style = MoeButtonStyle.TEXT)
                    }
                    else -> CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground, strokeWidth = 2.dp)
                }
            }
            if (qr != null) {
                Spacer(Modifier.height(16.dp))
                Text(qr.displayName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.fadeUpOnAppear(1))
                Text("@${qr.username}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.extended.textSecondary)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = qr.amount?.display ?: stringResource(Res.string.qr_any_amount),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MoeButton(
                    text = stringResource(if (qr?.amount == null) Res.string.qr_add_amount else Res.string.qr_edit_amount),
                    onClick = { onAction(MyQrAction.OnEditAmountClick) },
                    style = MoeButtonStyle.TEXT,
                )
                if (qr?.amount != null) {
                    MoeButton(stringResource(Res.string.qr_clear_amount), { onAction(MyQrAction.OnClearAmount) }, style = MoeButtonStyle.TEXT)
                }
            }
            MoeTextField(
                state = state.noteState,
                placeholder = stringResource(Res.string.qr_note_placeholder),
                inputTransformation = InputTransformation.maxLength(NOTE_MAX),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                onKeyboardAction = { onAction(MyQrAction.OnApplyNote) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))
        }
        Column(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            MoeButton(
                text = stringResource(Res.string.qr_share),
                onClick = { shareText?.let(share) },
                enabled = shareText != null,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            MoeButton(
                text = stringResource(if (state.isCopied) Res.string.qr_copied else Res.string.qr_copy),
                onClick = {
                    qr?.let {
                        copy(it.payload)
                        haptics.success()
                        onAction(MyQrAction.OnCopied)
                    }
                },
                enabled = qr != null,
                style = MoeButtonStyle.SECONDARY,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            )
        }
    }

    MoeSheet(
        visible = state.isAmountSheetVisible,
        onDismissRequest = { onAction(MyQrAction.OnDismissAmountSheet) },
        size = MoeSheetSize.LARGE,
    ) {
        AmountEntryContent(
            amount = state.draftAmount,
            onKey = { onAction(MyQrAction.OnKey(it)) },
            rejectKey = state.rejectKey,
            helper = null,
            isHelperError = false,
            continueLabel = stringResource(Res.string.qr_set_amount),
            canContinue = true,
            onContinue = { onAction(MyQrAction.OnApplyAmount) },
            modifier = Modifier.weight(1f),
        )
    }
}
