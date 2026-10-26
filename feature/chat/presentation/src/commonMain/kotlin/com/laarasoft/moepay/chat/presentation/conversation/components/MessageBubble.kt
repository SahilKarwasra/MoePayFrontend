package com.laarasoft.moepay.chat.presentation.conversation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.chat.domain.ChatMessage
import com.laarasoft.moepay.chat.domain.MessageContent
import com.laarasoft.moepay.chat.presentation.components.DeliveryTicks
import com.laarasoft.moepay.chat.presentation.resources.Res
import com.laarasoft.moepay.chat.presentation.resources.decline
import com.laarasoft.moepay.chat.presentation.resources.edited
import com.laarasoft.moepay.chat.presentation.resources.message_deleted_mine
import com.laarasoft.moepay.chat.presentation.resources.message_deleted_theirs
import com.laarasoft.moepay.chat.presentation.resources.not_delivered_tap
import com.laarasoft.moepay.chat.presentation.resources.original_message
import com.laarasoft.moepay.chat.presentation.resources.pay
import com.laarasoft.moepay.chat.presentation.resources.payment_failed
import com.laarasoft.moepay.chat.presentation.resources.payment_paid_you
import com.laarasoft.moepay.chat.presentation.resources.payment_you_paid
import com.laarasoft.moepay.chat.presentation.resources.request_requested_from_you
import com.laarasoft.moepay.chat.presentation.resources.request_sending
import com.laarasoft.moepay.chat.presentation.resources.request_you_requested
import com.laarasoft.moepay.chat.presentation.resources.you
import com.laarasoft.moepay.chat.presentation.timeline.BubblePosition
import com.laarasoft.moepay.chat.presentation.timeline.MessageStatus
import com.laarasoft.moepay.chat.presentation.timeline.ReplyPreview
import com.laarasoft.moepay.chat.presentation.timeline.TimelineItem
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.icon.painter
import com.laarasoft.moepay.core.designsystem.native.rememberHaptics
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.domain.money.MoneyRequestAction
import com.laarasoft.moepay.core.domain.money.MoneyRequestStatus
import com.laarasoft.moepay.core.domain.money.TransactionStatus
import com.laarasoft.moepay.core.presentation.util.formatTime
import com.laarasoft.moepay.core.presentation.util.toUiText
import com.laarasoft.moepay.core.ui.MoneyRequestStatusPill
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private val BIG = 20.dp
private val SMALL = 6.dp
private val REPLY_THRESHOLD = 64.dp

/** One message row: bubble or money card, aligned to its sender's side. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageRow(
    item: TimelineItem.Message,
    peerName: String,
    canReply: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onSwipeReply: () -> Unit,
    onPayRequest: (String) -> Unit,
    onDeclineRequest: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val message = item.message
    val haptics = rememberHaptics()
    val density = LocalDensity.current
    val thresholdPx = with(density) { REPLY_THRESHOLD.toPx() }
    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val topGap = if (item.position == BubblePosition.SINGLE || item.position == BubblePosition.FIRST) 8.dp else 2.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topGap)
            .then(
                if (canReply) {
                    Modifier.pointerInput(message.localId) {
                        var crossed = false
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (crossed) onSwipeReply()
                                crossed = false
                                scope.launch { offset.animateTo(0f, spring(dampingRatio = 0.7f, stiffness = 500f)) }
                            },
                            onDragCancel = { scope.launch { offset.animateTo(0f, spring()) } },
                        ) { change, dragAmount ->
                            change.consume()
                            // Resistance past the threshold, like the system's swipe actions.
                            val next = (offset.value + dragAmount * if (offset.value > thresholdPx) 0.3f else 1f).coerceIn(0f, thresholdPx * 1.6f)
                            scope.launch { offset.snapTo(next) }
                            if (!crossed && next >= thresholdPx) {
                                crossed = true
                                haptics.selection()
                            } else if (crossed && next < thresholdPx) {
                                crossed = false
                            }
                        }
                    }
                } else {
                    Modifier
                }
            ),
    ) {
        val maxBubble = maxWidth * 0.78f
        // Reply arrow revealed behind the bubble while swiping.
        Icon(
            painter = MoeIcon.Reply.painter(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.extended.textSecondary,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
                .size(20.dp)
                .graphicsLayer {
                    val progress = (offset.value / thresholdPx).coerceIn(0f, 1f)
                    alpha = progress
                    scaleX = 0.6f + 0.4f * progress
                    scaleY = 0.6f + 0.4f * progress
                },
        )
        Column(
            horizontalAlignment = if (message.isMine) Alignment.End else Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offset.value.roundToInt(), 0) },
        ) {
            val interaction = remember { MutableInteractionSource() }
            val clickable = Modifier.combinedClickable(
                interactionSource = interaction,
                indication = null,
                onClick = onTap,
                onLongClick = {
                    haptics.tap()
                    onLongPress()
                },
            )
            when (val content = message.content) {
                is MessageContent.Payment -> PaymentCard(message, content, item.status, clickable.widthIn(max = maxBubble))
                is MessageContent.Request -> RequestCard(
                    message = message,
                    content = content,
                    status = item.status,
                    onPay = { onPayRequest(content.moneyRequestId) },
                    onDecline = { onDeclineRequest(content.moneyRequestId) },
                    modifier = clickable.widthIn(max = maxBubble),
                )
                else -> TextBubble(item, peerName, clickable.widthIn(max = maxBubble))
            }
            if (item.status == MessageStatus.FAILED) {
                Text(
                    text = message.failure?.toUiText()?.asString() ?: stringResource(Res.string.not_delivered_tap),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp, end = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun TextBubble(item: TimelineItem.Message, peerName: String, modifier: Modifier) {
    val message = item.message
    val colors = MaterialTheme.colorScheme
    val mine = message.isMine
    val deleted = message.content is MessageContent.Deleted
    val background = when {
        deleted -> Color.Transparent
        mine -> colors.onBackground
        else -> colors.extended.fieldBackground
    }
    val foreground = if (mine && !deleted) colors.background else colors.onBackground
    val meta = if (mine && !deleted) colors.background.copy(alpha = 0.6f) else colors.extended.textTertiary
    val shape = bubbleShape(mine, item.position)

    Column(
        modifier = modifier
            .background(background, shape)
            .then(if (deleted) Modifier.border(1.dp, colors.extended.border, shape) else Modifier)
            .padding(start = 14.dp, end = 12.dp, top = 8.dp, bottom = 6.dp),
    ) {
        item.reply?.let { ReplyQuote(it, peerName, mine) }
        when (val content = message.content) {
            is MessageContent.Text -> Text(content.body, style = MaterialTheme.typography.bodyLarge, color = foreground)
            else -> Text(
                text = stringResource(if (mine) Res.string.message_deleted_mine else Res.string.message_deleted_theirs),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = colors.extended.textSecondary,
            )
        }
        MetaLine(message, item.status, meta, Modifier.align(Alignment.End))
    }
}

@Composable
private fun MetaLine(message: ChatMessage, status: MessageStatus, color: Color, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(top = 2.dp),
    ) {
        if (message.isEdited && message.content !is MessageContent.Deleted) {
            Text(stringResource(Res.string.edited), style = MaterialTheme.typography.labelSmall, color = color)
        }
        Text(message.createdAt.formatTime(), style = MaterialTheme.typography.labelSmall, color = color)
        if (status != MessageStatus.NONE && status != MessageStatus.FAILED) DeliveryTicks(status, tint = color, size = 13.dp)
    }
}

@Composable
private fun ReplyQuote(reply: ReplyPreview, peerName: String, inMine: Boolean) {
    val colors = MaterialTheme.colorScheme
    val accent = if (inMine) colors.background else colors.onBackground
    Row(
        modifier = Modifier
            .padding(bottom = 6.dp)
            .height(IntrinsicSize.Min)
            .background(accent.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .padding(end = 10.dp),
    ) {
        Box(Modifier.width(3.dp).fillMaxHeight().background(accent.copy(alpha = 0.7f), RoundedCornerShape(2.dp)))
        Column(Modifier.padding(start = 8.dp, top = 5.dp, bottom = 5.dp)) {
            Text(
                text = if (reply.isMine) stringResource(Res.string.you) else peerName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                maxLines = 1,
            )
            Text(
                text = reply.text ?: stringResource(Res.string.original_message),
                style = MaterialTheme.typography.bodySmall,
                color = accent.copy(alpha = 0.75f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PaymentCard(message: ChatMessage, content: MessageContent.Payment, status: MessageStatus, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val mine = content.isOutgoing
    val failed = content.status == TransactionStatus.FAILED
    val background = if (mine) colors.onBackground else colors.extended.fieldBackground
    val foreground = if (mine) colors.background else colors.onBackground
    val secondary = foreground.copy(alpha = 0.6f)
    Column(
        modifier = modifier
            .width(232.dp)
            .background(background, RoundedCornerShape(22.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(26.dp).background(foreground.copy(alpha = 0.12f), CircleShape),
            ) {
                Icon(
                    painter = (if (mine) MoeIcon.Send else MoeIcon.Request).painter(),
                    contentDescription = null,
                    tint = foreground,
                    modifier = Modifier.size(15.dp),
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(if (mine) Res.string.payment_you_paid else Res.string.payment_paid_you),
                style = MaterialTheme.typography.labelLarge,
                color = secondary,
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(content.amount.display, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = foreground)
        content.note?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = foreground.copy(alpha = 0.85f))
        }
        if (failed) {
            Spacer(Modifier.height(6.dp))
            Text(stringResource(Res.string.payment_failed), style = MaterialTheme.typography.labelMedium, color = colors.error)
        }
        MetaLine(message, status, secondary, Modifier.align(Alignment.End).padding(top = 6.dp))
    }
}

@Composable
private fun RequestCard(
    message: ChatMessage,
    content: MessageContent.Request,
    status: MessageStatus,
    onPay: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val outgoing = content.isOutgoing
    val sending = content.moneyRequestId.isEmpty()
    val canAct = !outgoing && content.status == MoneyRequestStatus.PENDING
    Column(
        modifier = modifier
            .width(248.dp)
            .background(colors.background, RoundedCornerShape(22.dp))
            .border(1.dp, colors.extended.border, RoundedCornerShape(22.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(if (outgoing) Res.string.request_you_requested else Res.string.request_requested_from_you),
                style = MaterialTheme.typography.labelLarge,
                color = colors.extended.textSecondary,
                modifier = Modifier.weight(1f),
            )
            if (!sending) MoneyRequestStatusPill(content.status)
        }
        Spacer(Modifier.height(8.dp))
        Text(content.amount.display, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = colors.onBackground)
        content.note?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = colors.extended.textSecondary)
        }
        if (sending && status != MessageStatus.FAILED) {
            Spacer(Modifier.height(6.dp))
            Text(stringResource(Res.string.request_sending), style = MaterialTheme.typography.labelMedium, color = colors.extended.textTertiary)
        }
        if (canAct) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (MoneyRequestAction.DECLINE in content.actions) {
                    CardButton(stringResource(Res.string.decline), filled = false, onClick = onDecline, modifier = Modifier.weight(1f))
                }
                if (MoneyRequestAction.PAY in content.actions) {
                    CardButton(stringResource(Res.string.pay), filled = true, onClick = onPay, modifier = Modifier.weight(1f))
                }
            }
        }
        MetaLine(message, status, colors.extended.textTertiary, Modifier.align(Alignment.End).padding(top = 6.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CardButton(text: String, filled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(38.dp)
            .background(if (filled) colors.onBackground else colors.extended.fieldBackground, CircleShape)
            .combinedClickable(onClick = onClick),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = if (filled) colors.background else colors.onBackground)
    }
}

/** Big outer corners, tight corners where the bubble joins its neighbours in a run. */
private fun bubbleShape(mine: Boolean, position: BubblePosition): RoundedCornerShape {
    val joinsAbove = position == BubblePosition.MIDDLE || position == BubblePosition.LAST
    val joinsBelow = position == BubblePosition.FIRST || position == BubblePosition.MIDDLE
    val top = if (joinsAbove) SMALL else BIG
    val bottom = if (joinsBelow) SMALL else BIG
    return if (mine) {
        RoundedCornerShape(topStart = BIG, topEnd = top, bottomEnd = bottom, bottomStart = BIG)
    } else {
        RoundedCornerShape(topStart = top, topEnd = BIG, bottomEnd = BIG, bottomStart = bottom)
    }
}
