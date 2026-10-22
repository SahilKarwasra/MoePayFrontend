package com.laarasoft.moepay.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.theme.MoeTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * The MoePay mark: a rounded tile with a single-stroke "M".
 * Same geometry as the Android launcher/splash vectors and the iOS LaunchLogo asset.
 */
@Composable
fun MoeLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    tileColor: Color = MaterialTheme.colorScheme.onBackground,
    markColor: Color = MaterialTheme.colorScheme.background,
) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension
        drawRoundRect(
            color = tileColor,
            cornerRadius = CornerRadius(s * 0.28f, s * 0.28f),
        )
        val m = Path().apply {
            moveTo(s * 0.30f, s * 0.68f)
            lineTo(s * 0.30f, s * 0.34f)
            lineTo(s * 0.50f, s * 0.56f)
            lineTo(s * 0.70f, s * 0.34f)
            lineTo(s * 0.70f, s * 0.68f)
        }
        drawPath(
            path = m,
            color = markColor,
            style = Stroke(width = s * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Composable
fun MoeWordmark(
    modifier: Modifier = Modifier,
    logoSize: Dp = 32.dp,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MoeLogo(size = logoSize)
        Text(
            text = "MoePay",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview
@Composable
private fun MoeLogoPreview() {
    MoeTheme { MoeWordmark() }
}
