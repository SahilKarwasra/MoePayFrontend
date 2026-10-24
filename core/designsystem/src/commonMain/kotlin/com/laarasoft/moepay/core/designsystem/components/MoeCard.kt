package com.laarasoft.moepay.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.icon.painter
import com.laarasoft.moepay.core.designsystem.motion.pressScale
import com.laarasoft.moepay.core.designsystem.theme.extended

/** An inset-grouped container, like an iOS grouped table section. */
@Composable
fun MoeCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.extended.fieldBackground, MaterialTheme.shapes.large)
            .padding(contentPadding),
        content = content,
    )
}

/** A hairline between rows inside a [MoeCard], inset past the leading element. */
@Composable
fun MoeRowDivider(startInset: Dp = 72.dp) {
    HorizontalDivider(
        modifier = Modifier.padding(start = startInset),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.extended.border,
    )
}

@Composable
fun MoeSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.extended.textSecondary,
                modifier = Modifier
                    .clickable(onClick = onAction)
                    .padding(vertical = 8.dp, horizontal = 4.dp),
            )
        }
    }
}

/** A tappable row: leading element, title + subtitle, trailing element or chevron. */
@Composable
fun MoeListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    showChevron: Boolean = false,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    onClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.pressScale(interactionSource, 0.985f).clickable(interactionSource, null, onClick = onClick) else Modifier)
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(14.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.extended.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
        if (showChevron) {
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = MoeIcon.ChevronRight.painter(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.extended.textTertiary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** A round icon tile used as a row's leading element. */
@Composable
fun MoeIconTile(
    icon: MoeIcon,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    inverted: Boolean = false,
    tint: Color? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .background(if (inverted) colorScheme.onBackground else colorScheme.background, CircleShape)
            .then(if (inverted) Modifier else Modifier.border(1.dp, colorScheme.extended.border, CircleShape)),
    ) {
        Icon(
            painter = icon.painter(),
            contentDescription = null,
            tint = tint ?: if (inverted) colorScheme.background else colorScheme.onBackground,
            modifier = Modifier.size(size * 0.45f),
        )
    }
}

enum class MoePillTone {
    NEUTRAL,
    POSITIVE,
    NEGATIVE,
    PENDING,
}

@Composable
fun MoeStatusPill(
    text: String,
    tone: MoePillTone,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val (container, content) = when (tone) {
        MoePillTone.NEUTRAL -> colorScheme.extended.fieldBackground to colorScheme.extended.textSecondary
        MoePillTone.POSITIVE -> colorScheme.onBackground to colorScheme.background
        MoePillTone.NEGATIVE -> colorScheme.error.copy(alpha = 0.12f) to colorScheme.error
        MoePillTone.PENDING -> colorScheme.background to colorScheme.onBackground
    }
    Box(
        modifier = modifier
            .background(container, CircleShape)
            .then(if (tone == MoePillTone.PENDING) Modifier.border(1.dp, colorScheme.extended.border, CircleShape) else Modifier)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = content)
    }
}

@Composable
fun MoeFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .pressScale(interactionSource)
            .background(if (selected) colorScheme.onBackground else colorScheme.background, CircleShape)
            .border(1.dp, if (selected) colorScheme.onBackground else colorScheme.extended.border, CircleShape)
            .clickable(interactionSource, null, onClick = onClick)
            .heightIn(min = 36.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colorScheme.background else colorScheme.onBackground,
        )
    }
}

/** A prompt that leads somewhere, e.g. "Set your wallet PIN". */
@Composable
fun MoeBanner(
    title: String,
    message: String,
    icon: MoeIcon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interactionSource, 0.985f)
            .border(1.dp, MaterialTheme.colorScheme.extended.border, MaterialTheme.shapes.large)
            .clickable(interactionSource, null, onClick = onClick)
            .padding(16.dp),
    ) {
        MoeIconTile(icon = icon, inverted = true, size = 40.dp)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
            Text(text = message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.extended.textSecondary)
        }
        Icon(
            painter = MoeIcon.ChevronRight.painter(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.extended.textTertiary,
            modifier = Modifier.size(20.dp),
        )
    }
}
