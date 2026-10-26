package com.laarasoft.moepay.chat.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.chat.domain.ChatMessage
import com.laarasoft.moepay.chat.domain.Conversation
import com.laarasoft.moepay.chat.domain.Delivery
import com.laarasoft.moepay.chat.domain.MessageContent
import com.laarasoft.moepay.chat.presentation.resources.Res
import com.laarasoft.moepay.chat.presentation.resources.preview_deleted
import com.laarasoft.moepay.chat.presentation.resources.preview_draft
import com.laarasoft.moepay.chat.presentation.resources.preview_failed
import com.laarasoft.moepay.chat.presentation.resources.preview_payment_received
import com.laarasoft.moepay.chat.presentation.resources.preview_payment_sent
import com.laarasoft.moepay.chat.presentation.resources.preview_request_received
import com.laarasoft.moepay.chat.presentation.resources.preview_request_sent
import com.laarasoft.moepay.chat.presentation.resources.preview_you
import com.laarasoft.moepay.chat.presentation.resources.yesterday
import com.laarasoft.moepay.chat.presentation.timeline.MessageStatus
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.icon.painter
import com.laarasoft.moepay.core.presentation.util.formatShort
import com.laarasoft.moepay.core.presentation.util.formatTime
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Instant

/** Clock while sending, one check when delivered, two when read, a red mark if it failed. */
@Composable
fun DeliveryTicks(
    status: MessageStatus,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 14.dp,
) {
    AnimatedContent(
        targetState = status,
        transitionSpec = { (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.6f)) togetherWith fadeOut(tween(120)) },
        label = "ticks",
        modifier = modifier,
    ) { target ->
        when (target) {
            MessageStatus.NONE -> Unit
            MessageStatus.PENDING -> Icon(MoeIcon.Clock.painter(), null, tint = tint, modifier = Modifier.size(size - 2.dp))
            MessageStatus.SENT -> Icon(MoeIcon.Check.painter(), null, tint = tint, modifier = Modifier.size(size))
            MessageStatus.READ -> Box(Modifier.width(size + 5.dp)) {
                Icon(MoeIcon.Check.painter(), null, tint = tint, modifier = Modifier.size(size))
                Icon(MoeIcon.Check.painter(), null, tint = tint, modifier = Modifier.size(size).offset(x = 5.dp))
            }
            MessageStatus.FAILED -> Icon(MoeIcon.Error.painter(), null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(size))
        }
    }
}

/** Three dots rising in a wave, iMessage-style. */
@Composable
fun TypingDots(
    color: Color,
    modifier: Modifier = Modifier,
    dotSize: Dp = 6.dp,
) {
    val transition = rememberInfiniteTransition(label = "typing")
    Row(horizontalArrangement = Arrangement.spacedBy(dotSize * 0.6f), verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        repeat(3) { index ->
            val lift by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 420),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 140),
                ),
                label = "dot$index",
            )
            Box(
                Modifier
                    .size(dotSize)
                    .graphicsLayer {
                        translationY = -lift * dotSize.toPx() * 0.6f
                        alpha = 0.45f + 0.55f * lift
                    }
                    .background(color, CircleShape)
            )
        }
    }
}

/** The second line of a chat row. */
@Composable
fun conversationPreview(conversation: Conversation): String {
    val message = conversation.lastMessage
    if (conversation.draft.isNotBlank()) return stringResource(Res.string.preview_draft, conversation.draft.trim())
    if (message == null) return ""
    return messagePreview(message)
}

@Composable
fun messagePreview(message: ChatMessage): String {
    if (message.delivery == Delivery.FAILED) return stringResource(Res.string.preview_failed)
    return when (val content = message.content) {
        is MessageContent.Text -> if (message.isMine) stringResource(Res.string.preview_you, content.body) else content.body
        is MessageContent.Payment -> stringResource(
            if (content.isOutgoing) Res.string.preview_payment_sent else Res.string.preview_payment_received,
            content.amount.display,
        )
        is MessageContent.Request -> stringResource(
            if (content.isOutgoing) Res.string.preview_request_sent else Res.string.preview_request_received,
            content.amount.display,
        )
        MessageContent.Deleted -> stringResource(Res.string.preview_deleted)
    }.replace('\n', ' ')
}

/** "14:05" today, "Yesterday", the weekday this week, otherwise "12 Oct". */
@Composable
fun listTimestamp(instant: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val today = Clock.System.todayIn(zone)
    val date = instant.toLocalDateTime(zone).date
    return when {
        date == today -> instant.formatTime(zone)
        date == today.minus(DatePeriod(days = 1)) -> stringResource(Res.string.yesterday)
        today.minus(DatePeriod(days = 6)) <= date -> date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        else -> date.formatShort(today)
    }
}
