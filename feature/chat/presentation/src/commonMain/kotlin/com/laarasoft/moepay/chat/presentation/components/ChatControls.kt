package com.laarasoft.moepay.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.icon.painter
import com.laarasoft.moepay.core.designsystem.motion.pressScale
import com.laarasoft.moepay.core.designsystem.theme.extended

/** A round icon button on a soft fill, like the iOS 26 toolbar buttons. */
@Composable
fun ChatIconButton(
    icon: MoeIcon,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    inverted: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .pressScale(interaction, 0.9f)
            .background(if (inverted) colors.onBackground else colors.extended.fieldBackground, CircleShape)
            .clickable(interaction, null, enabled = enabled, onClickLabel = contentDescription, onClick = onClick),
    ) {
        Icon(
            painter = icon.painter(),
            contentDescription = contentDescription,
            tint = (if (inverted) colors.background else colors.onBackground).copy(alpha = if (enabled) 1f else 0.35f),
            modifier = Modifier.size(size * 0.48f),
        )
    }
}

/** Rounded search field in the system style. */
@Composable
fun ChatSearchField(
    state: TextFieldState,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(colors.extended.fieldBackground, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
    ) {
        Icon(MoeIcon.Search.painter(), null, tint = colors.extended.textTertiary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (state.text.isEmpty()) {
                Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = colors.extended.textTertiary, maxLines = 1)
            }
            BasicTextField(
                state = state,
                lineLimits = TextFieldLineLimits.SingleLine,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onBackground),
                cursorBrush = SolidColor(colors.onBackground),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Black capsule with the unread count. */
@Composable
fun UnreadBadge(count: Int, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
            .background(colors.onBackground, CircleShape)
            .padding(horizontal = 7.dp),
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = colors.background,
            maxLines = 1,
        )
    }
}

/** "Waiting for network…" / "Connecting…" capsule. */
@Composable
fun ConnectionChip(text: String, showSpinner: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(colors.extended.fieldBackground, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        if (showSpinner) {
            CircularProgressIndicator(strokeWidth = 1.5.dp, color = colors.extended.textSecondary, modifier = Modifier.size(12.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium, color = colors.extended.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
