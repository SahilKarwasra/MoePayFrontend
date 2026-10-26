package com.laarasoft.moepay.chat.presentation.conversation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.chat.domain.ChatConnection
import com.laarasoft.moepay.chat.domain.ChatPolicy
import com.laarasoft.moepay.chat.domain.Delivery
import com.laarasoft.moepay.chat.domain.MessageContent
import com.laarasoft.moepay.chat.presentation.components.ChatIconButton
import com.laarasoft.moepay.chat.presentation.conversation.components.Composer
import com.laarasoft.moepay.chat.presentation.conversation.components.ComposerContext
import com.laarasoft.moepay.chat.presentation.conversation.components.ConversationIntro
import com.laarasoft.moepay.chat.presentation.conversation.components.DaySeparator
import com.laarasoft.moepay.chat.presentation.conversation.components.MessageRow
import com.laarasoft.moepay.chat.presentation.conversation.components.TypingBubble
import com.laarasoft.moepay.chat.presentation.conversation.components.UnreadDivider
import com.laarasoft.moepay.chat.presentation.money.ChatMoneyAction
import com.laarasoft.moepay.chat.presentation.money.ChatMoneyEvent
import com.laarasoft.moepay.chat.presentation.money.ChatMoneyMode
import com.laarasoft.moepay.chat.presentation.money.ChatMoneySheets
import com.laarasoft.moepay.chat.presentation.money.ChatMoneyViewModel
import com.laarasoft.moepay.chat.presentation.resources.Res
import com.laarasoft.moepay.chat.presentation.resources.action_copy
import com.laarasoft.moepay.chat.presentation.resources.action_delete
import com.laarasoft.moepay.chat.presentation.resources.action_discard
import com.laarasoft.moepay.chat.presentation.resources.action_edit
import com.laarasoft.moepay.chat.presentation.resources.action_reply
import com.laarasoft.moepay.chat.presentation.resources.action_request_money
import com.laarasoft.moepay.chat.presentation.resources.action_retry
import com.laarasoft.moepay.chat.presentation.resources.action_send_money
import com.laarasoft.moepay.chat.presentation.resources.action_view_details
import com.laarasoft.moepay.chat.presentation.resources.back
import com.laarasoft.moepay.chat.presentation.resources.cancel
import com.laarasoft.moepay.chat.presentation.resources.delete
import com.laarasoft.moepay.chat.presentation.resources.delete_message
import com.laarasoft.moepay.chat.presentation.resources.delete_title
import com.laarasoft.moepay.chat.presentation.resources.editing_message
import com.laarasoft.moepay.chat.presentation.resources.jump_to_latest
import com.laarasoft.moepay.chat.presentation.resources.money_offline
import com.laarasoft.moepay.chat.presentation.resources.replying_to
import com.laarasoft.moepay.chat.presentation.resources.status_connecting
import com.laarasoft.moepay.chat.presentation.resources.status_online_username
import com.laarasoft.moepay.chat.presentation.resources.status_waiting_network
import com.laarasoft.moepay.chat.presentation.resources.typing
import com.laarasoft.moepay.chat.presentation.resources.you
import com.laarasoft.moepay.chat.presentation.timeline.TimelineItem
import com.laarasoft.moepay.chat.presentation.timeline.previewText
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.native.MoeActionSheet
import com.laarasoft.moepay.core.designsystem.native.MoeAlertDialog
import com.laarasoft.moepay.core.designsystem.native.MoeBarAction
import com.laarasoft.moepay.core.designsystem.native.MoeDialogButton
import com.laarasoft.moepay.core.designsystem.native.MoeDialogButtonStyle
import com.laarasoft.moepay.core.designsystem.native.MoeSheetAction
import com.laarasoft.moepay.core.designsystem.native.MoeTopBar
import com.laarasoft.moepay.core.designsystem.native.rememberClipboard
import com.laarasoft.moepay.core.designsystem.native.rememberHaptics
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Clock

@Composable
fun ConversationRoot(
    conversationId: String,
    onBack: () -> Unit,
    onOpenRequest: (String) -> Unit,
    onOpenTransaction: (String) -> Unit,
    onOpenResetPin: () -> Unit,
    viewModel: ConversationViewModel = koinViewModel(key = "conversation-$conversationId") { parametersOf(conversationId) },
    moneyViewModel: ChatMoneyViewModel = koinViewModel(key = "money-$conversationId") { parametersOf(conversationId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val moneyState by moneyViewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val clipboard = rememberClipboard()
    val haptics = rememberHaptics()

    // Read receipts only while the chat is actually on screen.
    LifecycleResumeEffect(Unit) {
        viewModel.onAction(ConversationAction.OnVisibilityChanged(true))
        onPauseOrDispose { viewModel.onAction(ConversationAction.OnVisibilityChanged(false)) }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ConversationEvent.Back -> onBack()
            is ConversationEvent.OpenRequest -> onOpenRequest(event.moneyRequestId)
            is ConversationEvent.OpenTransaction -> onOpenTransaction(event.transactionId)
            is ConversationEvent.Copy -> {
                clipboard(event.text)
                haptics.success()
            }
            is ConversationEvent.OpenMoney -> moneyViewModel.onAction(
                ChatMoneyAction.OnOpen(if (event.isSend) ChatMoneyMode.SEND else ChatMoneyMode.REQUEST)
            )
            ConversationEvent.ScrollToLatest -> scope.launch { listState.animateScrollToItem(0) }
        }
    }
    ObserveAsEvents(moneyViewModel.events) { event ->
        when (event) {
            ChatMoneyEvent.Done -> {
                haptics.success()
                scope.launch { listState.animateScrollToItem(0) }
            }
            ChatMoneyEvent.OpenResetPin -> onOpenResetPin()
        }
    }

    ConversationScreen(state = state, onAction = viewModel::onAction, listState = listState)
    ChatMoneySheets(state = moneyState, onAction = moneyViewModel::onAction)
}

@Composable
fun ConversationScreen(
    state: ConversationState,
    onAction: (ConversationAction) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
) {
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    val subtitle = when {
        state.isPeerTyping -> stringResource(Res.string.typing)
        state.connection == ChatConnection.OFFLINE -> stringResource(Res.string.status_waiting_network)
        state.connection == ChatConnection.CONNECTING -> stringResource(Res.string.status_connecting)
        state.peerUsername != null -> stringResource(Res.string.status_online_username, state.peerUsername)
        else -> null
    }

    // Keep the newest message in view when it arrives and the user is already at the bottom.
    val newestKey = state.timeline.firstOrNull()?.key
    LaunchedEffect(newestKey) {
        if (listState.firstVisibleItemIndex <= 2) listState.animateScrollToItem(0)
    }
    // Load older messages as the top comes into view.
    LaunchedEffect(listState, state.timeline.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .distinctUntilChanged()
            .filter { it >= state.timeline.size - 6 && state.timeline.isNotEmpty() }
            .collect { onAction(ConversationAction.OnLoadOlder) }
    }
    val showJump by remember { derivedStateOf { listState.firstVisibleItemIndex > 4 } }

    Column(Modifier.fillMaxSize().background(colors.background)) {
        MoeTopBar(
            title = state.peerName,
            subtitle = subtitle,
            navigationAction = MoeBarAction(MoeIcon.Back, stringResource(Res.string.back)) { onAction(ConversationAction.OnBack) },
        )
        Box(Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn(
                state = listState,
                reverseLayout = true,
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                if (state.isPeerTyping) {
                    item(key = "typing") { TypingBubble(Modifier.animateItem()) }
                }
                items(state.timeline, key = { it.key }, contentType = { it::class.simpleName }) { item ->
                    when (item) {
                        is TimelineItem.Message -> MessageRow(
                            item = item,
                            peerName = state.peerName,
                            canReply = ChatPolicy.canReply(item.message),
                            onTap = { onAction(ConversationAction.OnMessageTap(item.message)) },
                            onLongPress = { onAction(ConversationAction.OnMessageLongPress(item.message)) },
                            onSwipeReply = { onAction(ConversationAction.OnReply(item.message)) },
                            onPayRequest = { onAction(ConversationAction.OnOpenRequest(it)) },
                            onDeclineRequest = { onAction(ConversationAction.OnOpenRequest(it)) },
                            modifier = Modifier.animateItem(),
                        )
                        is TimelineItem.DaySeparator -> DaySeparator(item.date, Modifier.animateItem())
                        TimelineItem.UnreadDivider -> UnreadDivider(Modifier.animateItem())
                    }
                }
                if (state.isLoadingOlder) {
                    item(key = "older") {
                        Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 2.dp, color = colors.extended.textTertiary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                if (state.hasLoaded && state.timeline.isEmpty() && !state.isPeerTyping) {
                    item(key = "intro") { ConversationIntro(state.peerName) }
                }
            }
            // The plain overload: inside this Box we don't want the enclosing Column's variant.
            androidx.compose.animation.AnimatedVisibility(
                visible = showJump,
                enter = fadeIn() + scaleIn(initialScale = 0.6f),
                exit = fadeOut() + scaleOut(targetScale = 0.6f),
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            ) {
                ChatIconButton(
                    icon = MoeIcon.Back,
                    contentDescription = stringResource(Res.string.jump_to_latest),
                    onClick = { scope.launch { listState.animateScrollToItem(0) } },
                    // The back chevron turned to point down.
                    modifier = Modifier.graphicsLayer { rotationZ = -90f },
                    size = 40.dp,
                )
            }
        }
        Composer(
            state = state.composer,
            context = composerContext(state),
            isEditing = state.editing != null,
            onSend = { onAction(ConversationAction.OnSend) },
            onMoney = { onAction(ConversationAction.OnMoneyClick) },
            onCancelContext = {
                onAction(if (state.editing != null) ConversationAction.OnCancelEdit else ConversationAction.OnCancelReply)
            },
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars).imePadding(),
        )
    }

    MessageMenu(state, onAction)

    if (state.isMoneyMenuVisible) {
        MoeActionSheet(
            actions = listOf(
                MoeSheetAction(stringResource(Res.string.action_send_money), { onAction(ConversationAction.OnSendMoneyClick) }),
                MoeSheetAction(stringResource(Res.string.action_request_money), { onAction(ConversationAction.OnRequestMoneyClick) }),
            ),
            cancelLabel = stringResource(Res.string.cancel),
            onDismissRequest = { onAction(ConversationAction.OnDismissMoneyMenu) },
        )
    }
    if (state.isOfflineMoneyNoticeVisible) {
        MoeAlertDialog(
            title = stringResource(Res.string.money_offline),
            message = null,
            confirmButton = MoeDialogButton("OK", { onAction(ConversationAction.OnDismissOfflineMoneyNotice) }),
            onDismissRequest = { onAction(ConversationAction.OnDismissOfflineMoneyNotice) },
        )
    }
    state.confirmDeleteFor?.let {
        MoeAlertDialog(
            title = stringResource(Res.string.delete_title),
            message = stringResource(Res.string.delete_message),
            confirmButton = MoeDialogButton(stringResource(Res.string.delete), { onAction(ConversationAction.OnConfirmDelete) }, MoeDialogButtonStyle.DESTRUCTIVE),
            dismissButton = MoeDialogButton(stringResource(Res.string.cancel), { onAction(ConversationAction.OnDismissDelete) }, MoeDialogButtonStyle.CANCEL),
            onDismissRequest = { onAction(ConversationAction.OnDismissDelete) },
        )
    }
}

/** The long-press menu: only the actions that apply to this message right now. */
@Composable
private fun MessageMenu(state: ConversationState, onAction: (ConversationAction) -> Unit) {
    val message = state.menuFor ?: return
    val now = Clock.System.now()
    val actions = buildList {
        if (message.delivery == Delivery.FAILED) {
            add(MoeSheetAction(stringResource(Res.string.action_retry), { onAction(ConversationAction.OnRetry(message)) }))
            add(MoeSheetAction(stringResource(Res.string.action_discard), { onAction(ConversationAction.OnDiscard(message)) }, MoeDialogButtonStyle.DESTRUCTIVE))
        } else {
            if (ChatPolicy.canReply(message)) add(MoeSheetAction(stringResource(Res.string.action_reply), { onAction(ConversationAction.OnReply(message)) }))
            if (message.content is MessageContent.Text) add(MoeSheetAction(stringResource(Res.string.action_copy), { onAction(ConversationAction.OnCopy(message)) }))
            if (message.content is MessageContent.Payment || message.content is MessageContent.Request) {
                add(MoeSheetAction(stringResource(Res.string.action_view_details), { onAction(ConversationAction.OnMessageTap(message)) }))
            }
            if (ChatPolicy.canEdit(message, now)) add(MoeSheetAction(stringResource(Res.string.action_edit), { onAction(ConversationAction.OnEdit(message)) }))
            if (ChatPolicy.canDelete(message, now)) {
                add(MoeSheetAction(stringResource(Res.string.action_delete), { onAction(ConversationAction.OnDelete(message)) }, MoeDialogButtonStyle.DESTRUCTIVE))
            }
        }
    }
    MoeActionSheet(
        actions = actions,
        cancelLabel = stringResource(Res.string.cancel),
        onDismissRequest = { onAction(ConversationAction.OnDismissMenu) },
        title = previewText(message)?.take(80),
    )
}

@Composable
private fun composerContext(state: ConversationState): ComposerContext? {
    state.editing?.let { return ComposerContext(stringResource(Res.string.editing_message), previewText(it).orEmpty(), MoeIcon.Edit) }
    state.replyTo?.let { reply ->
        val who = if (reply.isMine) stringResource(Res.string.you) else state.peerName
        return ComposerContext(stringResource(Res.string.replying_to, who), previewText(reply).orEmpty(), MoeIcon.Reply)
    }
    return null
}
