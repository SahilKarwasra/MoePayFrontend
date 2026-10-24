package com.laarasoft.moepay.realtime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.components.MoeAvatar
import com.laarasoft.moepay.core.designsystem.native.rememberHaptics
import com.laarasoft.moepay.core.domain.money.TransactionDirection
import com.laarasoft.moepay.core.domain.money.TransactionType
import com.laarasoft.moepay.core.domain.realtime.RealtimeEvent
import com.laarasoft.moepay.core.domain.realtime.RealtimeEvents
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

private data class BannerContent(
    val name: String,
    val title: String,
    val message: String,
    val target: BannerTarget,
)

sealed interface BannerTarget {
    data class Transaction(val id: String) : BannerTarget
    data class Request(val id: String) : BannerTarget
}

/**
 * A top banner when money arrives from someone or they ask for some. Own actions (sending, bank
 * transfers) already end on a success screen, so they don't show here.
 */
@Composable
fun RealtimeBanner(
    onOpen: (BannerTarget) -> Unit,
    modifier: Modifier = Modifier,
    realtimeEvents: RealtimeEvents = koinInject(),
) {
    var content by remember { mutableStateOf<BannerContent?>(null) }
    var visible by remember { mutableStateOf(false) }
    val haptics = rememberHaptics()

    LaunchedEffect(realtimeEvents) {
        realtimeEvents.events.collect { event ->
            val next = when (event) {
                is RealtimeEvent.WalletCredited -> {
                    val t = event.movement.transaction
                    val fromPerson = t.direction == TransactionDirection.IN &&
                        (t.type == TransactionType.P2P_TRANSFER || t.type == TransactionType.REQUEST_PAYMENT)
                    if (!fromPerson) null else BannerContent(
                        name = t.counterparty.displayName,
                        title = "${t.amount.display} from ${t.counterparty.displayName}",
                        message = t.note ?: t.title,
                        target = BannerTarget.Transaction(t.id),
                    )
                }
                is RealtimeEvent.MoneyRequestReceived -> BannerContent(
                    name = event.request.counterparty.displayName,
                    title = "${event.request.counterparty.displayName} requested ${event.request.amount.display}",
                    message = event.request.note ?: "Tap to review",
                    target = BannerTarget.Request(event.request.id),
                )
                else -> null
            }
            if (next != null) {
                content = next
                visible = true
                haptics.success()
            }
        }
    }
    LaunchedEffect(content, visible) {
        if (visible) {
            delay(4_000L)
            visible = false
        }
    }

    AnimatedVisibility(
        visible = visible && content != null,
        enter = slideInVertically(spring(dampingRatio = 0.8f)) { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars),
    ) {
        val banner = content ?: return@AnimatedVisibility
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth()
                .shadow(16.dp, MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.onBackground, MaterialTheme.shapes.large)
                .clickable {
                    visible = false
                    onOpen(banner.target)
                }
                .padding(14.dp),
        ) {
            MoeAvatar(name = banner.name, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(banner.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.background, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(banner.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
