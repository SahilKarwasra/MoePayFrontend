package com.laarasoft.moepay.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.motion.rememberShakeController
import com.laarasoft.moepay.core.designsystem.motion.shake
import com.laarasoft.moepay.core.designsystem.native.rememberHaptics
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.presentation.money.AmountKey

const val PIN_LENGTH = 6

/**
 * Six-digit PIN entry that fills the space it's given: header centred in the free space above,
 * keypad anchored to the bottom. The digits stay inside this composable and are handed to
 * [onComplete] once, when the sixth is typed. Increment [errorKey] to clear, shake and buzz
 * after a rejected PIN.
 */
@Composable
fun MoePinPad(
    title: String,
    onComplete: (pin: String) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    errorText: String? = null,
    errorKey: Int = 0,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    icon: MoeIcon = MoeIcon.Lock,
    /** 1-based position in a multi-step flow (e.g. create → confirm); null hides the indicator. */
    step: Int? = null,
    totalSteps: Int = 0,
    hint: String? = null,
    footer: @Composable () -> Unit = {},
) {
    var pin by remember { mutableStateOf("") }
    val shake = rememberShakeController()
    val haptics = rememberHaptics()

    LaunchedEffect(errorKey) {
        if (errorKey > 0) {
            haptics.error()
            shake.shake()
            pin = ""
        }
    }
    LaunchedEffect(isLoading) {
        // A finished request that didn't error leaves the dots full; clear them for the next use.
        if (!isLoading && errorText == null && pin.length == PIN_LENGTH) pin = ""
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.weight(1f))
        if (step != null && totalSteps > 1) {
            MoeStepIndicator(current = step, total = totalSteps, modifier = Modifier.width(72.dp))
            Spacer(Modifier.height(24.dp))
        }
        MoeIconTile(icon = icon, size = 64.dp, inverted = true)
        Spacer(Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.extended.textSecondary,
            textAlign = TextAlign.Center,
            minLines = 2,
        )
        Spacer(Modifier.height(28.dp))
        Box(contentAlignment = Alignment.Center, modifier = Modifier.height(24.dp)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            } else {
                MoePinDots(filled = pin.length, isError = errorText != null, modifier = Modifier.shake(shake))
            }
        }
        Box(Modifier.heightIn(min = 40.dp).padding(top = 14.dp), contentAlignment = Alignment.TopCenter) {
            if (errorText != null) {
                Text(
                    text = errorText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.extended.textTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
        MoeKeypad(
            allowDecimal = false,
            enabled = enabled && !isLoading,
            onKey = { key ->
                when (key) {
                    AmountKey.Backspace -> pin = pin.dropLast(1)
                    is AmountKey.Digit -> if (pin.length < PIN_LENGTH) {
                        pin += key.value
                        if (pin.length == PIN_LENGTH) onComplete(pin)
                    }
                    AmountKey.Decimal -> Unit
                }
            },
        )
        footer()
    }
}

@Composable
fun MoePinDots(
    filled: Int,
    modifier: Modifier = Modifier,
    length: Int = PIN_LENGTH,
    isError: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = modifier) {
        repeat(length) { index ->
            val isFilled = index < filled
            val scale by animateFloatAsState(
                targetValue = if (isFilled) 1f else 0.8f,
                animationSpec = spring(dampingRatio = 0.45f, stiffness = 900f),
                label = "pinDot$index",
            )
            val color = if (isError) colorScheme.error else colorScheme.onBackground
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .then(
                        if (isFilled) Modifier.background(color, CircleShape)
                        else Modifier.border(1.5.dp, if (isError) colorScheme.error else colorScheme.extended.borderStrong.copy(alpha = 0.35f), CircleShape)
                    ),
            )
        }
    }
}
