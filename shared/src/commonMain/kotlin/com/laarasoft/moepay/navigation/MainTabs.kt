package com.laarasoft.moepay.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.chat.domain.ChatRepository
import com.laarasoft.moepay.chat.presentation.list.ChatListRoot
import org.koin.compose.koinInject
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.native.LocalBottomBarInset
import com.laarasoft.moepay.core.designsystem.native.MoeNavigationBar
import com.laarasoft.moepay.core.designsystem.native.MoeNavigationItem
import com.laarasoft.moepay.core.designsystem.native.rememberHaptics
import com.laarasoft.moepay.home.presentation.home.HomeDestination
import com.laarasoft.moepay.home.presentation.home.HomeRoot
import com.laarasoft.moepay.wallet.presentation.tab.WalletDestination
import com.laarasoft.moepay.wallet.presentation.tab.WalletTabRoot

private enum class MainTab { HOME, WALLET, CHAT }

/** Home · eWallet · Chat over the native tab bar. Pushed screens cover the tab bar, like iOS. */
@Composable
fun MainTabs(
    onHomeNavigate: (HomeDestination) -> Unit,
    onWalletNavigate: (WalletDestination) -> Unit,
    onOpenConversation: (conversationId: String) -> Unit,
    onNewChat: () -> Unit,
    chatRepository: ChatRepository = koinInject(),
) {
    val unreadChats by chatRepository.observeTotalUnread().collectAsStateWithLifecycle(initialValue = 0)
    var selected by rememberSaveable { mutableIntStateOf(MainTab.HOME.ordinal) }
    val haptics = rememberHaptics()

    val density = LocalDensity.current
    var barHeight by remember { mutableStateOf(0.dp) }

    // The bar floats over the content (iOS draws only its glass capsule), so tab content runs
    // to the bottom edge and pads itself by LocalBottomBarInset.
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CompositionLocalProvider(LocalBottomBarInset provides barHeight) {
            AnimatedContent(
                targetState = MainTab.entries[selected],
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "mainTab",
                modifier = Modifier.fillMaxSize(),
            ) { tab ->
                when (tab) {
                    MainTab.HOME -> HomeRoot(onNavigate = onHomeNavigate)
                    MainTab.WALLET -> WalletTabRoot(onNavigate = onWalletNavigate)
                    MainTab.CHAT -> ChatListRoot(onOpenConversation = onOpenConversation, onNewChat = onNewChat)
                }
            }
        }
        MoeNavigationBar(
            items = listOf(
                MoeNavigationItem("Home", MoeIcon.Home, MoeIcon.HomeFilled),
                MoeNavigationItem("eWallet", MoeIcon.Wallet, MoeIcon.WalletFilled),
                MoeNavigationItem("Chat", MoeIcon.Chat, MoeIcon.ChatFilled, badgeCount = unreadChats),
            ),
            selectedIndex = selected,
            onItemSelected = { index ->
                if (index != selected) haptics.selection()
                selected = index
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged { barHeight = with(density) { it.height.toDp() } },
        )
    }
}
