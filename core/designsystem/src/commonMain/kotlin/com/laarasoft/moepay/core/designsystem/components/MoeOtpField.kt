package com.laarasoft.moepay.core.designsystem.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.theme.extended

/** A row of [length] digit boxes backed by a single hidden text field. */
@Composable
fun MoeOtpField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    length: Int = 6,
    isError: Boolean = false,
    enabled: Boolean = true,
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val cursorAlpha by rememberInfiniteTransition(label = "otpCursor").animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "otpCursorAlpha",
    )

    BasicTextField(
        state = state,
        enabled = enabled,
        inputTransformation = DigitsOnlyTransformation.then(InputTransformation.maxLength(length)),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        interactionSource = interactionSource,
        cursorBrush = SolidColor(Color.Transparent),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        decorator = { innerTextField ->
            Box {
                Box(Modifier.alpha(0f)) { innerTextField() }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val text = state.text
                    repeat(length) { index ->
                        val char = text.getOrNull(index)
                        val isActive = isFocused && index == text.length.coerceAtMost(length - 1) &&
                            text.length < length
                        val borderColor = when {
                            isError -> colorScheme.error
                            isActive || char != null -> colorScheme.extended.borderStrong
                            else -> colorScheme.extended.fieldBackground
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.82f)
                                .background(colorScheme.extended.fieldBackground, MaterialTheme.shapes.medium)
                                .border(1.5.dp, borderColor, MaterialTheme.shapes.medium),
                        ) {
                            if (char != null) {
                                Text(
                                    text = char.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = colorScheme.onBackground,
                                )
                            } else if (isActive) {
                                Box(
                                    Modifier
                                        .width(2.dp)
                                        .height(24.dp)
                                        .alpha(cursorAlpha)
                                        .background(colorScheme.onBackground)
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}
