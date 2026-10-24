package com.laarasoft.moepay.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.icon.painter
import com.laarasoft.moepay.core.designsystem.motion.pressScale

/**
 * The black balance card showing the server's own [balanceDisplay]; changes roll in digit by
 * digit. [balancePence] null means loading.
 */
@Composable
fun MoeBalanceCard(
    label: String,
    balancePence: Long?,
    balanceDisplay: String?,
    isHidden: Boolean,
    onToggleHidden: () -> Unit,
    toggleDescription: String,
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val onCard = colorScheme.background

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colorScheme.onBackground, MaterialTheme.shapes.extraLarge)
            .padding(24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = onCard.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .background(onCard.copy(alpha = 0.12f), CircleShape)
                    .clickable(onClickLabel = toggleDescription, onClick = onToggleHidden),
            ) {
                Icon(
                    painter = (if (isHidden) MoeIcon.EyeOff else MoeIcon.Eye).painter(),
                    contentDescription = toggleDescription,
                    tint = onCard,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        if (balancePence == null || balanceDisplay == null) {
            MoeSkeletonBlock(width = 180.dp, height = 44.dp, onDark = true)
        } else {
            AnimatedContent(
                targetState = isHidden,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                label = "balanceHidden",
            ) { hidden ->
                if (hidden) {
                    Text(text = "£ ••••••", style = MaterialTheme.typography.displayMedium, color = onCard)
                } else {
                    RollingAmount(pence = balancePence, display = balanceDisplay)
                }
            }
        }
        footer()
    }
}

/**
 * Shows [display] as is on first appearance; when the balance changes, only the characters that
 * differ roll vertically (up for a rise, down for a fall). Tabular figures keep the width steady.
 */
@Composable
private fun RollingAmount(pence: Long, display: String) {
    val lastPence = remember { longArrayOf(pence) }
    val isRise = remember(pence) { (pence >= lastPence[0]).also { lastPence[0] = pence } }
    val style = MaterialTheme.typography.displayMedium.copy(fontFeatureSettings = "tnum")
    val color = MaterialTheme.colorScheme.background

    Row {
        display.forEachIndexed { index, char ->
            // Keyed from the right so "£999.00" -> "£1,000.00" adds characters on the left.
            key(display.length - index) {
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        val direction = if (isRise) 1 else -1
                        (slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it * direction } + fadeIn(tween(220))) togetherWith
                            (slideOutVertically(tween(320, easing = FastOutSlowInEasing)) { -it * direction } + fadeOut(tween(160))) using
                            SizeTransform(clip = true)
                    },
                    label = "balanceChar",
                ) { target ->
                    Text(text = target.toString(), style = style, color = color, maxLines = 1)
                }
            }
        }
    }
}

/** A circular action on dark or light backgrounds, e.g. "Send". */
@Composable
fun MoeCardAction(
    icon: MoeIcon,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    MoeQuickAction(icon = icon, label = label, onClick = onClick, modifier = modifier, enabled = enabled, onDark = true)
}

@Composable
fun MoeQuickAction(
    icon: MoeIcon,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDark: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val tileColor = if (onDark) colorScheme.background.copy(alpha = 0.14f) else colorScheme.onBackground
    val iconColor = colorScheme.background
    val labelColor = if (onDark) colorScheme.background else colorScheme.onBackground
    val alpha = if (enabled) 1f else 0.4f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .pressScale(interactionSource, 0.94f)
            .clickable(interactionSource, null, onClick = onClick)
            .padding(vertical = 4.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .background(tileColor.copy(alpha = tileColor.alpha * alpha), CircleShape),
        ) {
            Icon(
                painter = icon.painter(),
                contentDescription = null,
                tint = iconColor.copy(alpha = alpha),
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = labelColor.copy(alpha = alpha),
            maxLines = 1,
        )
    }
}

@Composable
fun MoeActionRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier.fillMaxWidth(),
    ) { content() }
}
