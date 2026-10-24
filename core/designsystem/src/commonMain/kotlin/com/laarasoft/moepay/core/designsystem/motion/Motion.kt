package com.laarasoft.moepay.core.designsystem.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/** Drives a short horizontal shake, used for a wrong PIN or an invalid amount key. */
@Stable
class ShakeController {
    internal val offset = Animatable(0f)

    suspend fun shake() {
        for (target in listOf(14f, -12f, 9f, -6f, 3f, 0f)) {
            offset.animateTo(target, tween(durationMillis = 45))
        }
    }
}

@Composable
fun rememberShakeController(): ShakeController = remember { ShakeController() }

fun Modifier.shake(controller: ShakeController): Modifier =
    graphicsLayer { translationX = controller.offset.value * density }

/** Scales slightly while pressed, like a native button. */
fun Modifier.pressScale(interactionSource: InteractionSource, pressedScale: Float = 0.97f): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) pressedScale else 1f, label = "pressScale")
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Fades and lifts content in once, staggered by [index]. Use on a screen's top-level sections;
 * it only runs the first time the section enters composition.
 */
fun Modifier.fadeUpOnAppear(index: Int = 0): Modifier = composed {
    val progress = remember { Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 55L)
        progress.animateTo(1f, tween(durationMillis = 420, easing = androidx.compose.animation.core.FastOutSlowInEasing))
    }
    graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 18f * density
    }
}
