package com.laarasoft.moepay.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.laarasoft.moepay.chat.presentation.resources.Res
import com.laarasoft.moepay.chat.presentation.resources.chat
import com.laarasoft.moepay.chat.presentation.resources.chat_empty_message
import com.laarasoft.moepay.chat.presentation.resources.chat_empty_title
import com.laarasoft.moepay.core.designsystem.components.MoeEmptyState
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.native.LocalBottomBarInset
import com.laarasoft.moepay.core.designsystem.native.MoeTopBar
import com.laarasoft.moepay.core.designsystem.theme.MoeTheme
import org.jetbrains.compose.resources.stringResource

/** Placeholder tab; chat isn't built yet. */
@Composable
fun ChatRoot() {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        MoeTopBar(title = stringResource(Res.string.chat))
        Box(Modifier.weight(1f).padding(bottom = LocalBottomBarInset.current), contentAlignment = Alignment.Center) {
            MoeEmptyState(
                icon = MoeIcon.Chat,
                title = stringResource(Res.string.chat_empty_title),
                message = stringResource(Res.string.chat_empty_message),
            )
        }
    }
}

@Preview
@Composable
private fun ChatPreview() {
    MoeTheme { ChatRoot() }
}
