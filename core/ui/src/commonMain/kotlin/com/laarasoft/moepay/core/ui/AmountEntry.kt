package com.laarasoft.moepay.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.components.MoeAmountDisplay
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeKeypad
import com.laarasoft.moepay.core.designsystem.motion.rememberShakeController
import com.laarasoft.moepay.core.designsystem.native.rememberHaptics
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.presentation.money.AmountInput
import com.laarasoft.moepay.core.presentation.money.AmountKey
import com.laarasoft.moepay.core.presentation.util.UiText
import com.laarasoft.moepay.core.ui.resources.Res
import com.laarasoft.moepay.core.ui.resources.delete
import org.jetbrains.compose.resources.stringResource

/**
 * The amount step shared by send, request, add money, withdraw and QR: a header slot (who or
 * which bank), the big amount, a helper line, the keypad and the continue button.
 *
 * [rejectKey] increments whenever a key press was refused, to shake the amount.
 */
@Composable
fun AmountEntryContent(
    amount: AmountInput,
    onKey: (AmountKey) -> Unit,
    rejectKey: Int,
    helper: UiText?,
    isHelperError: Boolean,
    continueLabel: String,
    canContinue: Boolean,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    amountLocked: Boolean = false,
    header: @Composable ColumnScope.() -> Unit = {},
    belowAmount: @Composable ColumnScope.() -> Unit = {},
) {
    val shake = rememberShakeController()
    val haptics = rememberHaptics()
    LaunchedEffect(rejectKey) {
        if (rejectKey > 0) {
            haptics.error()
            shake.shake()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp),
    ) {
        header()
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MoeAmountDisplay(
                    amount = amount.display,
                    shakeController = shake,
                    isPlaceholder = amount.text.isEmpty(),
                )
                AnimatedContent(
                    targetState = helper to isHelperError,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "amountHelper",
                ) { (text, isError) ->
                    Text(
                        text = text?.asString().orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.extended.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
                belowAmount()
            }
        }
        if (!amountLocked) {
            MoeKeypad(onKey = onKey, backspaceDescription = stringResource(Res.string.delete))
            Spacer(Modifier.height(12.dp))
        }
        MoeButton(
            text = continueLabel,
            onClick = onContinue,
            enabled = canContinue,
            isLoading = isLoading,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
    }
}
