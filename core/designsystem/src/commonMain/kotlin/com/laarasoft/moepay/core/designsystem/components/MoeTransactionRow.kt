package com.laarasoft.moepay.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import com.laarasoft.moepay.core.designsystem.theme.extended

/**
 * One line of history. [amount] is the server's signed display ("+£5.00" / "-£5.00").
 * Failed movements are struck through in the error colour.
 */
@Composable
fun MoeTransactionRow(
    title: String,
    subtitle: String,
    amount: String,
    isIncoming: Boolean,
    isFailed: Boolean,
    leading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    trailingCaption: String? = null,
    onClick: (() -> Unit)? = null,
) {
    MoeListRow(
        title = title,
        subtitle = subtitle,
        leading = leading,
        onClick = onClick,
        modifier = modifier,
        trailing = { TransactionAmount(amount, isIncoming, isFailed, trailingCaption) },
    )
}

@Composable
private fun RowScope.TransactionAmount(amount: String, isIncoming: Boolean, isFailed: Boolean, caption: String?) {
    val colorScheme = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = amount,
            style = MaterialTheme.typography.titleSmall,
            color = when {
                isFailed -> colorScheme.error
                isIncoming -> colorScheme.onBackground
                else -> colorScheme.onBackground
            },
            textDecoration = if (isFailed) TextDecoration.LineThrough else null,
            maxLines = 1,
        )
        if (caption != null) {
            Text(text = caption, style = MaterialTheme.typography.labelSmall, color = colorScheme.extended.textTertiary)
        }
    }
}
