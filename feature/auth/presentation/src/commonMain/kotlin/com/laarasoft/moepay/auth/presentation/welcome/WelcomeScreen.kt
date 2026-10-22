package com.laarasoft.moepay.auth.presentation.welcome

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.auth.presentation.resources.Res
import com.laarasoft.moepay.auth.presentation.resources.welcome_card_balance
import com.laarasoft.moepay.auth.presentation.resources.welcome_get_started
import com.laarasoft.moepay.auth.presentation.resources.welcome_subtitle
import com.laarasoft.moepay.auth.presentation.resources.welcome_terms
import com.laarasoft.moepay.auth.presentation.resources.welcome_title
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeLogo
import com.laarasoft.moepay.core.designsystem.components.MoeWordmark
import com.laarasoft.moepay.core.designsystem.theme.MoeTheme
import com.laarasoft.moepay.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource

@Composable
fun WelcomeRoot(
    onGetStartedClick: () -> Unit,
) {
    WelcomeScreen(onGetStartedClick = onGetStartedClick)
}

@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        MoeWordmark(logoSize = 28.dp)

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            CardStack(modifier = Modifier.fillMaxWidth(0.82f))
        }

        Text(
            text = stringResource(Res.string.welcome_title),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(Res.string.welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.extended.textSecondary,
        )
        Spacer(Modifier.height(32.dp))
        MoeButton(
            text = stringResource(Res.string.welcome_get_started),
            onClick = onGetStartedClick,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.welcome_terms),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.extended.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Monochrome card illustration with a slow float. */
@Composable
private fun CardStack(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val float by rememberInfiniteTransition(label = "cardFloat").animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(2600), RepeatMode.Reverse),
        label = "cardFloatOffset",
    )

    Box(modifier = modifier.aspectRatio(1.1f), contentAlignment = Alignment.Center) {
        CardOutline(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .rotate(-12f)
                .graphicsLayer { translationY = -float * 0.6f },
        )
        CardOutline(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .rotate(-5f)
                .graphicsLayer { translationY = -float * 0.3f },
        )
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.586f)
                .graphicsLayer { translationY = float }
                .rotate(3f)
                .background(colorScheme.onBackground, MaterialTheme.shapes.large)
                .padding(20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                MoeLogo(
                    size = 28.dp,
                    tileColor = colorScheme.background,
                    markColor = colorScheme.onBackground,
                )
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    Box(Modifier.size(22.dp).border(1.5.dp, colorScheme.background, CircleShape))
                    Box(Modifier.size(22.dp).border(1.5.dp, colorScheme.background, CircleShape))
                }
            }
            Column {
                Text(
                    text = stringResource(Res.string.welcome_card_balance),
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.background.copy(alpha = 0.6f),
                )
                Text(
                    text = "₹ 24,500.00",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorScheme.background,
                )
            }
            Text(
                text = "••••  ••••  ••••  2048",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.background.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun CardOutline(modifier: Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1.586f)
            .background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.large)
            .border(1.5.dp, MaterialTheme.colorScheme.extended.border, MaterialTheme.shapes.large),
    )
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    MoeTheme { WelcomeScreen(onGetStartedClick = {}) }
}
