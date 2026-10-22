package com.laarasoft.moepay.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.theme.MoeTheme
import com.laarasoft.moepay.core.designsystem.theme.extended
import androidx.compose.ui.tooling.preview.Preview

enum class MoeButtonStyle {
    PRIMARY,
    SECONDARY,
    TEXT,
}

@Composable
fun MoeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: MoeButtonStyle = MoeButtonStyle.PRIMARY,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "buttonScale")
    val colorScheme = MaterialTheme.colorScheme

    val colors = when (style) {
        MoeButtonStyle.PRIMARY -> ButtonDefaults.buttonColors(
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onPrimary,
            disabledContainerColor = colorScheme.extended.disabledFill,
            disabledContentColor = colorScheme.extended.textDisabled,
        )
        MoeButtonStyle.SECONDARY -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = colorScheme.onBackground,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = colorScheme.extended.textDisabled,
        )
        MoeButtonStyle.TEXT -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = colorScheme.onBackground,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = colorScheme.extended.textDisabled,
        )
    }
    val border = if (style == MoeButtonStyle.SECONDARY) {
        BorderStroke(1.dp, if (enabled) colorScheme.extended.border else colorScheme.extended.disabledFill)
    } else null

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        colors = colors,
        border = border,
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 24.dp),
        interactionSource = interactionSource,
        modifier = modifier
            .height(if (style == MoeButtonStyle.TEXT) 44.dp else 56.dp)
            .scale(scale),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.alpha(if (isLoading) 0f else 1f),
            )
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = if (style == MoeButtonStyle.PRIMARY) colorScheme.onPrimary else colorScheme.onBackground,
                )
            }
        }
    }
}

@Preview
@Composable
private fun MoeButtonPreview() {
    MoeTheme {
        MoeButton(text = "Continue", onClick = {}, modifier = Modifier.fillMaxWidth())
    }
}
