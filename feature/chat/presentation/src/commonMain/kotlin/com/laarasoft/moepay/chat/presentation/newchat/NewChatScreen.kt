package com.laarasoft.moepay.chat.presentation.newchat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.chat.domain.ChatConnection
import com.laarasoft.moepay.chat.presentation.components.ChatSearchField
import com.laarasoft.moepay.chat.presentation.resources.Res
import com.laarasoft.moepay.chat.presentation.resources.back
import com.laarasoft.moepay.chat.presentation.resources.new_chat_hint
import com.laarasoft.moepay.chat.presentation.resources.new_chat_offline
import com.laarasoft.moepay.chat.presentation.resources.new_chat_placeholder
import com.laarasoft.moepay.chat.presentation.resources.new_chat_recent
import com.laarasoft.moepay.chat.presentation.resources.new_message
import com.laarasoft.moepay.chat.presentation.resources.start_chat_with
import com.laarasoft.moepay.core.designsystem.components.MoeAvatar
import com.laarasoft.moepay.core.designsystem.components.MoeIconTile
import com.laarasoft.moepay.core.designsystem.components.MoeSectionHeader
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.native.MoeBarAction
import com.laarasoft.moepay.core.designsystem.native.MoeTopBar
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NewChatRoot(
    onBack: () -> Unit,
    onOpenConversation: (String) -> Unit,
    username: String? = null,
    userId: String? = null,
    viewModel: NewChatViewModel = koinViewModel(key = "new-chat-$username-$userId") { parametersOf(NewChatTarget(username, userId)) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            NewChatEvent.Back -> onBack()
            is NewChatEvent.OpenConversation -> onOpenConversation(event.conversationId)
        }
    }
    NewChatScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun NewChatScreen(
    state: NewChatState,
    onAction: (NewChatAction) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

    Column(Modifier.fillMaxSize().background(colors.background)) {
        MoeTopBar(
            title = stringResource(Res.string.new_message),
            navigationAction = MoeBarAction(MoeIcon.Back, stringResource(Res.string.back)) { onAction(NewChatAction.OnBack) },
        )
        ChatSearchField(
            state = state.queryState,
            placeholder = stringResource(Res.string.new_chat_placeholder),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).focusRequester(focusRequester),
        )
        LazyColumn(Modifier.weight(1f).windowInsetsPadding(WindowInsets.navigationBars).imePadding()) {
            item(key = "target") {
                AnimatedVisibility(
                    visible = state.target != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !state.isOpening && state.connection != ChatConnection.OFFLINE) { onAction(NewChatAction.OnStartChat) }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                    ) {
                        MoeIconTile(MoeIcon.Compose, inverted = true)
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text = stringResource(Res.string.start_chat_with, state.targetLabel),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = colors.onBackground,
                            modifier = Modifier.weight(1f),
                        )
                        if (state.isOpening) {
                            CircularProgressIndicator(strokeWidth = 2.dp, color = colors.onBackground, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            item(key = "hint") {
                val message = when {
                    state.error != null -> state.error.asString()
                    state.target != null && state.connection == ChatConnection.OFFLINE -> stringResource(Res.string.new_chat_offline)
                    state.target == null && state.queryState.text.isNotEmpty() && state.recent.isEmpty() -> stringResource(Res.string.new_chat_hint)
                    else -> null
                }
                if (message != null) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (state.error != null) colors.error else colors.extended.textSecondary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }
            }
            if (state.recent.isNotEmpty()) {
                item(key = "recent-header") {
                    MoeSectionHeader(title = stringResource(Res.string.new_chat_recent), modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp))
                }
                items(state.recent, key = { it.id }) { conversation ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .clickable { onAction(NewChatAction.OnConversationClick(conversation.id)) }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                    ) {
                        MoeAvatar(name = conversation.peer.displayName, size = 44.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(conversation.peer.displayName, style = MaterialTheme.typography.bodyLarge, color = colors.onBackground, maxLines = 1)
                            conversation.peer.username?.let {
                                Text("@$it", style = MaterialTheme.typography.bodySmall, color = colors.extended.textSecondary, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}
