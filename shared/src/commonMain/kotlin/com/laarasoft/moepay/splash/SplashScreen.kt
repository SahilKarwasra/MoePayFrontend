package com.laarasoft.moepay.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.components.MoeLogo
import com.laarasoft.moepay.core.designsystem.theme.MoeTheme
import com.laarasoft.moepay.core.designsystem.theme.extended
import kotlinx.coroutines.launch

/**
 * Starts exactly where the native launch screen leaves off (96dp logo, centred, theme
 * background), then lifts the logo and reveals the wordmark.
 */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val logoOffset = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffset = remember { Animatable(12f) }

    LaunchedEffect(Unit) {
        val easing = tween<Float>(durationMillis = 650, easing = FastOutSlowInEasing)
        launch { logoOffset.animateTo(-36f, easing) }
        launch {
            textAlpha.animateTo(1f, tween(durationMillis = 500, delayMillis = 250))
        }
        launch { textOffset.animateTo(0f, tween(durationMillis = 500, delayMillis = 250, easing = FastOutSlowInEasing)) }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        MoeLogo(
            size = 96.dp,
            modifier = Modifier.graphicsLayer { translationY = logoOffset.value * density },
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                alpha = textAlpha.value
                translationY = (84f + textOffset.value) * density
            },
        ) {
            Text(
                text = "MoePay",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Money, made simple.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textSecondary,
            )
        }
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    MoeTheme { SplashScreen() }
}
