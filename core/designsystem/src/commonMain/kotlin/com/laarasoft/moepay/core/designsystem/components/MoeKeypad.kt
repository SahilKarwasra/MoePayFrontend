package com.laarasoft.moepay.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.icon.painter
import com.laarasoft.moepay.core.designsystem.motion.ShakeController
import com.laarasoft.moepay.core.designsystem.motion.shake
import com.laarasoft.moepay.core.designsystem.native.rememberHaptics
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.presentation.money.AmountKey

/** A phone-style numeric keypad. The decimal key only appears when [allowDecimal]. */
@Composable
fun MoeKeypad(
    onKey: (AmountKey) -> Unit,
    modifier: Modifier = Modifier,
    allowDecimal: Boolean = true,
    enabled: Boolean = true,
    backspaceDescription: String = "Delete",
) {
    val haptics = rememberHaptics()
    val press: (AmountKey) -> Unit = { key ->
        if (enabled) {
            haptics.tap()
            onKey(key)
        }
    }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9)).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { digit -> KeypadKey(Modifier.weight(1f), { press(AmountKey.Digit(digit)) }) { KeyText(digit.toString()) } }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            if (allowDecimal) {
                KeypadKey(Modifier.weight(1f), { press(AmountKey.Decimal) }) { KeyText(".") }
            } else {
                Box(Modifier.weight(1f))
            }
            KeypadKey(Modifier.weight(1f), { press(AmountKey.Digit(0)) }) { KeyText("0") }
            KeypadKey(Modifier.weight(1f), { press(AmountKey.Backspace) }) {
                Icon(
                    painter = MoeIcon.Backspace.painter(),
                    contentDescription = backspaceDescription,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}

@Composable
private fun KeypadKey(modifier: Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(64.dp)
            .clickable(interactionSource, null, onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(if (pressed) MaterialTheme.colorScheme.extended.fieldBackground else Color.Transparent, CircleShape),
        )
        content()
    }
}

@Composable
private fun KeyText(text: String) {
    Text(text = text, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
}

/**
 * The big amount being typed. Each character rolls in, and the size steps down for long amounts.
 */
@Composable
fun MoeAmountDisplay(
    amount: String,
    shakeController: ShakeController,
    modifier: Modifier = Modifier,
    symbol: String = "£",
    isError: Boolean = false,
    isPlaceholder: Boolean = false,
) {
    val style: TextStyle = when {
        amount.length > 10 -> MaterialTheme.typography.displaySmall
        amount.length > 7 -> MaterialTheme.typography.displayMedium
        else -> MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.25f)
    }
    val color = when {
        isError -> MaterialTheme.colorScheme.error
        isPlaceholder -> MaterialTheme.colorScheme.extended.textDisabled
        else -> MaterialTheme.colorScheme.onBackground
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth().shake(shakeController),
    ) {
        Text(text = symbol, style = style, color = color)
        amount.forEachIndexed { index, char ->
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    (slideInVertically(tween(180)) { it / 2 } + fadeIn(tween(180))) togetherWith
                        (slideOutVertically(tween(140)) { -it / 2 } + fadeOut(tween(140)))
                },
                label = "amountChar$index",
            ) { c ->
                Text(text = c.toString(), style = style, color = color)
            }
        }
    }
}
