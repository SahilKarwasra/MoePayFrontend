package com.laarasoft.moepay.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laarasoft.moepay.core.designsystem.theme.extended

/** Monochrome initials avatar. */
@Composable
fun MoeAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    inverted: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            // Outlined like MoeIconTile so it stands out on cards as well as on the background.
            .background(if (inverted) colorScheme.onBackground else colorScheme.background, CircleShape)
            .then(if (inverted) Modifier else Modifier.border(1.dp, colorScheme.extended.border, CircleShape)),
    ) {
        Text(
            text = initials(name),
            style = TextStyle(fontSize = (size.value * 0.36f).sp, fontWeight = FontWeight.SemiBold),
            color = if (inverted) colorScheme.background else colorScheme.onBackground,
        )
    }
}

internal fun initials(name: String): String {
    val parts = name.trim().removePrefix("@").split(' ').filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
    }
}
