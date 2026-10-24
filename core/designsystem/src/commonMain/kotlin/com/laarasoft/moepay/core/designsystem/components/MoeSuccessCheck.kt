package com.laarasoft.moepay.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.native.rememberHaptics

/** A filled circle that springs in, then a check that draws itself. Buzzes success once. */
@Composable
fun MoeSuccessCheck(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    circleColor: Color = MaterialTheme.colorScheme.onBackground,
    checkColor: Color = MaterialTheme.colorScheme.background,
    isError: Boolean = false,
) {
    val circle = remember { Animatable(0f) }
    val stroke = remember { Animatable(0f) }
    val haptics = rememberHaptics()
    val fill = if (isError) MaterialTheme.colorScheme.error else circleColor

    LaunchedEffect(Unit) {
        circle.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow))
        if (isError) haptics.error() else haptics.success()
        stroke.animateTo(1f, tween(durationMillis = 380, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension
        scale(circle.value) {
            drawCircle(color = fill)
        }
        val mark = Path().apply {
            if (isError) {
                moveTo(s * 0.34f, s * 0.34f); lineTo(s * 0.66f, s * 0.66f)
                moveTo(s * 0.66f, s * 0.34f); lineTo(s * 0.34f, s * 0.66f)
            } else {
                moveTo(s * 0.29f, s * 0.52f); lineTo(s * 0.44f, s * 0.66f); lineTo(s * 0.72f, s * 0.37f)
            }
        }
        val measure = PathMeasure().apply { setPath(mark, false) }
        val partial = Path()
        measure.getSegment(0f, measure.length * stroke.value, partial, true)
        drawPath(partial, color = checkColor, style = Stroke(width = s * 0.075f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
