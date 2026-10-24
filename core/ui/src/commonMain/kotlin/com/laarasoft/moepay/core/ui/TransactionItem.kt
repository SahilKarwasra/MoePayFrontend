package com.laarasoft.moepay.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.laarasoft.moepay.core.designsystem.components.MoeAvatar
import com.laarasoft.moepay.core.designsystem.components.MoeIconTile
import com.laarasoft.moepay.core.designsystem.components.MoeTransactionRow
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.domain.money.PartyKind
import com.laarasoft.moepay.core.domain.money.Transaction
import com.laarasoft.moepay.core.domain.money.TransactionDirection
import com.laarasoft.moepay.core.domain.money.TransactionType
import com.laarasoft.moepay.core.presentation.util.formatTime
import com.laarasoft.moepay.core.presentation.util.toDayLabel
import com.laarasoft.moepay.core.presentation.util.toLocalDate
import com.laarasoft.moepay.core.ui.resources.Res
import com.laarasoft.moepay.core.ui.resources.status_failed
import org.jetbrains.compose.resources.stringResource

/**
 * A history row from a [Transaction]: the server's title, a time (or day and time), and the
 * signed amount. [showDay] adds the day for lists that aren't grouped by day.
 */
@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDay: Boolean = true,
) {
    val time = transaction.createdAt.formatTime()
    val whenText = if (showDay) "${transaction.createdAt.toLocalDate().toDayLabel().asString()}, $time" else time
    val subtitle = if (transaction.isFailed) {
        "${stringResource(Res.string.status_failed)} · $whenText"
    } else {
        transaction.note?.let { "$it · $whenText" } ?: whenText
    }
    MoeTransactionRow(
        title = transaction.title,
        subtitle = subtitle,
        amount = transaction.signedDisplay,
        isIncoming = transaction.direction == TransactionDirection.IN,
        isFailed = transaction.isFailed,
        leading = { TransactionLeading(transaction) },
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
fun TransactionLeading(transaction: Transaction) {
    when {
        transaction.counterparty.kind == PartyKind.BANK -> MoeIconTile(
            icon = if (transaction.type == TransactionType.WITHDRAW) MoeIcon.Withdraw else MoeIcon.Bank,
        )
        else -> MoeAvatar(name = transaction.counterparty.displayName)
    }
}
