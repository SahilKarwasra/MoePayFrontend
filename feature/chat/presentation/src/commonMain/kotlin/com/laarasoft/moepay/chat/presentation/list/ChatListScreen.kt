package com.laarasoft.moepay.chat.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.chat.domain.ChatConnection
import com.laarasoft.moepay.chat.domain.Conversation
import com.laarasoft.moepay.chat.presentation.components.ChatIconButton
import com.laarasoft.moepay.chat.presentation.components.ChatSearchField
import com.laarasoft.moepay.chat.presentation.components.ConnectionChip
import com.laarasoft.moepay.chat.presentation.components.DeliveryTicks
import com.laarasoft.moepay.chat.presentation.components.TypingDots
import com.laarasoft.moepay.chat.presentation.components.UnreadBadge
import com.laarasoft.moepay.chat.presentation.components.conversationPreview
import com.laarasoft.moepay.chat.presentation.components.listTimestamp
import com.laarasoft.moepay.chat.presentation.resources.Res
import com.laarasoft.moepay.chat.presentation.resources.chat
import com.laarasoft.moepay.chat.presentation.resources.chat_empty_message
import com.laarasoft.moepay.chat.presentation.resources.chat_empty_title
import com.laarasoft.moepay.chat.presentation.resources.new_message
import com.laarasoft.moepay.chat.presentation.resources.no_results
import com.laarasoft.moepay.chat.presentation.resources.search_chats
import com.laarasoft.moepay.chat.presentation.resources.start_a_chat
import com.laarasoft.moepay.chat.presentation.resources.status_connecting
import com.laarasoft.moepay.chat.presentation.resources.status_waiting_network
import com.laarasoft.moepay.chat.presentation.resources.typing
import com.laarasoft.moepay.chat.presentation.timeline.MessageStatus
import com.laarasoft.moepay.chat.presentation.timeline.statusOf
import com.laarasoft.moepay.core.designsystem.components.MoeAvatar
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeButtonStyle
import com.laarasoft.moepay.core.designsystem.components.MoeEmptyState
import com.laarasoft.moepay.core.designsystem.components.MoeLargeTitle
import com.laarasoft.moepay.core.designsystem.components.MoeSkeletonRows
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.motion.fadeUpOnAppear
import com.laarasoft.moepay.core.designsystem.native.LocalBottomBarInset
import com.laarasoft.moepay.core.designsystem.native.rememberHaptics
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatListRoot(
    onOpenConversation: (conversationId: String) -> Unit,
    onNewChat: () -> Unit,
    viewModel: ChatListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ChatListEvent.OpenConversation -> onOpenConversation(event.conversationId)
            ChatListEvent.OpenNewChat -> onNewChat()
        }
    }
    ChatListScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    state: ChatListState,
    onAction: (ChatListAction) -> Unit,
) {
    val haptics = rememberHaptics()
    val visible = state.visibleConversations

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onAction(ChatListAction.OnRefresh) },
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp + LocalBottomBarInset.current),
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars),
        ) {
            item(key = "title") {
                MoeLargeTitle(
                    title = stringResource(Res.string.chat),
                    modifier = Modifier.fadeUpOnAppear(0),
                    trailing = {
                        ChatIconButton(
                            icon = MoeIcon.Compose,
                            contentDescription = stringResource(Res.string.new_message),
                            onClick = {
                                haptics.tap()
                                onAction(ChatListAction.OnNewChatClick)
                            },
                        )
                    },
                )
            }
            if (state.connection != ChatConnection.ONLINE) {
                item(key = "connection") {
                    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 12.dp)) {
                        ConnectionChip(
                            text = stringResource(
                                if (state.connection == ChatConnection.OFFLINE) Res.string.status_waiting_network else Res.string.status_connecting
                            ),
                            showSpinner = state.connection == ChatConnection.CONNECTING,
                        )
                    }
                }
            }
            if (state.conversations.isNotEmpty()) {
                item(key = "search") {
                    ChatSearchField(
                        state = state.searchState,
                        placeholder = stringResource(Res.string.search_chats),
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp).fadeUpOnAppear(1),
                    )
                }
            }

            when {
                state.isLoading -> item(key = "loading") { MoeSkeletonRows(count = 6, modifier = Modifier.padding(horizontal = 4.dp)) }
                state.conversations.isEmpty() -> item(key = "empty") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(top = 96.dp).fadeUpOnAppear(1),
                    ) {
                        MoeEmptyState(
                            icon = MoeIcon.Chat,
                            title = stringResource(Res.string.chat_empty_title),
                            message = stringResource(Res.string.chat_empty_message),
                        )
                        Spacer(Modifier.height(24.dp))
                        MoeButton(
                            text = stringResource(Res.string.start_a_chat),
                            onClick = { onAction(ChatListAction.OnNewChatClick) },
                            style = MoeButtonStyle.SECONDARY,
                        )
                    }
                }
                visible.isEmpty() -> item(key = "no-results") {
                    Text(
                        text = stringResource(Res.string.no_results, state.query.trim()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.extended.textSecondary,
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                    )
                }
                else -> items(visible, key = { it.id }) { conversation ->
                    Column(Modifier.animateItem()) {
                        ConversationRow(
                            conversation = conversation,
                            isTyping = conversation.id in state.typingConversations,
                            onClick = { onAction(ChatListAction.OnConversationClick(conversation.id)) },
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.extended.border,
                            modifier = Modifier.padding(start = 88.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: Conversation,
    isTyping: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val unread = conversation.unreadCount > 0
    val last = conversation.lastMessage
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        MoeAvatar(name = conversation.peer.displayName, size = 54.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conversation.peer.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = listTimestamp(conversation.lastActivityAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (unread) colors.onBackground else colors.extended.textTertiary,
                    fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isTyping) {
                    TypingDots(color = colors.extended.textSecondary, dotSize = 5.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(Res.string.typing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.extended.textSecondary,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    val status = if (last != null && conversation.draft.isBlank()) statusOf(last, conversation.peerLastReadSeq) else MessageStatus.NONE
                    if (status != MessageStatus.NONE) {
                        DeliveryTicks(status = status, tint = colors.extended.textTertiary, size = 15.dp)
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        text = conversationPreview(conversation),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (unread) colors.onBackground else colors.extended.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (unread) {
                    Spacer(Modifier.width(10.dp))
                    UnreadBadge(conversation.unreadCount)
                }
            }
        }
    }
}
