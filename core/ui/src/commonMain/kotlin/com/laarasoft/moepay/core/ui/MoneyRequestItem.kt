package com.laarasoft.moepay.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.laarasoft.moepay.core.designsystem.components.MoeAvatar
import com.laarasoft.moepay.core.designsystem.components.MoeListRow
import com.laarasoft.moepay.core.designsystem.components.MoePillTone
import com.laarasoft.moepay.core.designsystem.components.MoeStatusPill
import com.laarasoft.moepay.core.domain.money.MoneyRequest
import com.laarasoft.moepay.core.domain.money.MoneyRequestDirection
import com.laarasoft.moepay.core.domain.money.MoneyRequestStatus
import com.laarasoft.moepay.core.presentation.util.toDayLabel
import com.laarasoft.moepay.core.presentation.util.toLocalDate
import com.laarasoft.moepay.core.ui.resources.Res
import com.laarasoft.moepay.core.ui.resources.request_from
import com.laarasoft.moepay.core.ui.resources.request_to
import com.laarasoft.moepay.core.ui.resources.status_cancelled
import com.laarasoft.moepay.core.ui.resources.status_declined
import com.laarasoft.moepay.core.ui.resources.status_expired
import com.laarasoft.moepay.core.ui.resources.status_paid
import com.laarasoft.moepay.core.ui.resources.status_pending
import org.jetbrains.compose.resources.stringResource

@Composable
fun MoneyRequestItem(
    request: MoneyRequest,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val name = request.counterparty.displayName
    val title = if (request.direction == MoneyRequestDirection.INCOMING) {
        stringResource(Res.string.request_from, name)
    } else {
        stringResource(Res.string.request_to, name)
    }
    val day = request.createdAt.toLocalDate().toDayLabel().asString()
    MoeListRow(
        title = "$title ${request.amount.display}",
        subtitle = request.note?.let { "$it · $day" } ?: day,
        leading = { MoeAvatar(name = name) },
        trailing = { MoneyRequestStatusPill(request.status) },
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
fun MoneyRequestStatusPill(status: MoneyRequestStatus) {
    val (text, tone) = when (status) {
        MoneyRequestStatus.PENDING -> stringResource(Res.string.status_pending) to MoePillTone.PENDING
        MoneyRequestStatus.PAID -> stringResource(Res.string.status_paid) to MoePillTone.POSITIVE
        MoneyRequestStatus.DECLINED -> stringResource(Res.string.status_declined) to MoePillTone.NEGATIVE
        MoneyRequestStatus.CANCELLED -> stringResource(Res.string.status_cancelled) to MoePillTone.NEUTRAL
        MoneyRequestStatus.EXPIRED -> stringResource(Res.string.status_expired) to MoePillTone.NEUTRAL
        MoneyRequestStatus.UNKNOWN -> return
    }
    MoeStatusPill(text = text, tone = tone)
}
