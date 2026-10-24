package com.laarasoft.moepay.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.theme.extended

/** A pulsing placeholder block shown while content loads. */
@Composable
fun MoeSkeletonBlock(
    width: Dp?,
    height: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    onDark: Boolean = false,
) {
    val pulse by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(850, easing = LinearEasing), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    val color = if (onDark) MaterialTheme.colorScheme.background.copy(alpha = 0.18f) else MaterialTheme.colorScheme.extended.border
    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .alpha(pulse)
            .background(color, shape),
    )
}

/** Placeholder rows matching [MoeTransactionRow]. */
@Composable
fun MoeSkeletonRows(count: Int = 3, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(count) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                MoeSkeletonBlock(width = 44.dp, height = 44.dp, shape = CircleShape)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoeSkeletonBlock(width = 150.dp, height = 14.dp)
                    MoeSkeletonBlock(width = 90.dp, height = 12.dp)
                }
                MoeSkeletonBlock(width = 64.dp, height = 14.dp)
            }
        }
    }
}

@Composable
fun MoeSkeletonCircle(size: Dp) = MoeSkeletonBlock(width = size, height = size, shape = CircleShape, modifier = Modifier.size(size))
