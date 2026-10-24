package com.laarasoft.moepay.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrPixelShape
import io.github.alexzhirkevich.qrose.options.roundCorners
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

/**
 * A scannable QR on a white tile (always dark-on-light so every scanner reads it), with the
 * MoePay mark in the middle.
 */
@Composable
fun MoeQrCode(
    data: String,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp,
    contentDescription: String? = null,
) {
    val painter = rememberQrCodePainter(data) {
        shapes {
            ball = QrBallShape.roundCorners(0.3f)
            frame = QrFrameShape.roundCorners(0.3f)
            darkPixel = QrPixelShape.roundCorners(0.4f)
        }
        colors {
            dark = QrBrush.solid(Color.Black)
            light = QrBrush.solid(Color.White)
        }
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(Color.White, MaterialTheme.shapes.large)
            .padding(16.dp),
    ) {
        Image(painter = painter, contentDescription = contentDescription, modifier = Modifier.size(size))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size * 0.22f).background(Color.White, MaterialTheme.shapes.medium),
        ) {
            MoeLogo(size = size * 0.17f, tileColor = Color.Black, markColor = Color.White)
        }
    }
}
