package com.laarasoft.moepay.chat.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.laarasoft.moepay.chat.presentation.conversation.ConversationRoot
import com.laarasoft.moepay.chat.presentation.newchat.NewChatRoot
import kotlinx.serialization.Serializable

sealed interface ChatRoutes {
    @Serializable
    data class Conversation(val conversationId: String) : ChatRoutes

    /** Start a chat; with a [username] or [userId] it opens that person's chat straight away. */
    @Serializable
    data class NewChat(val username: String? = null, val userId: String? = null) : ChatRoutes
}

/**
 * Screens pushed over the tabs from Chat. Money screens belong to other features, so the
 * app root says where a request, a payment or the PIN reset lives.
 */
fun NavGraphBuilder.chatDestinations(
    navController: NavController,
    onOpenRequest: (moneyRequestId: String) -> Unit,
    onOpenTransaction: (transactionId: String) -> Unit,
    onOpenResetPin: () -> Unit,
) {
    composable<ChatRoutes.Conversation> { entry ->
        val route = entry.toRoute<ChatRoutes.Conversation>()
        ConversationRoot(
            conversationId = route.conversationId,
            onBack = { navController.navigateUp() },
            onOpenRequest = onOpenRequest,
            onOpenTransaction = onOpenTransaction,
            onOpenResetPin = onOpenResetPin,
        )
    }
    composable<ChatRoutes.NewChat> { entry ->
        val route = entry.toRoute<ChatRoutes.NewChat>()
        NewChatRoot(
            username = route.username,
            userId = route.userId,
            onBack = { navController.navigateUp() },
            onOpenConversation = { id ->
                navController.navigate(ChatRoutes.Conversation(id)) {
                    popUpTo<ChatRoutes.NewChat> { inclusive = true }
                }
            },
        )
    }
}
