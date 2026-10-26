package com.laarasoft.moepay.chat.presentation.conversation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.chat.presentation.components.TypingDots
import com.laarasoft.moepay.chat.presentation.resources.Res
import com.laarasoft.moepay.chat.presentation.resources.say_hi
import com.laarasoft.moepay.chat.presentation.resources.say_hi_message
import com.laarasoft.moepay.chat.presentation.resources.unread_divider
import com.laarasoft.moepay.core.designsystem.components.MoeAvatar
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.presentation.util.toDayLabel
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
fun DaySeparator(date: LocalDate, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(top = 16.dp, bottom = 6.dp), contentAlignment = Alignment.Center) {
        Text(
            text = date.toDayLabel().asString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.extended.textSecondary,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.extended.fieldBackground, CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun UnreadDivider(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth().padding(vertical = 10.dp),
    ) {
        HorizontalDivider(Modifier.weight(1f), thickness = 0.5.dp, color = colors.extended.border)
        Text(stringResource(Res.string.unread_divider), style = MaterialTheme.typography.labelMedium, color = colors.extended.textSecondary)
        HorizontalDivider(Modifier.weight(1f), thickness = 0.5.dp, color = colors.extended.border)
    }
}

/** The peer's typing bubble at the bottom of the conversation. */
@Composable
fun TypingBubble(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(top = 8.dp)) {
        Box(
            Modifier
                .background(MaterialTheme.colorScheme.extended.fieldBackground, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            TypingDots(color = MaterialTheme.colorScheme.extended.textSecondary)
        }
    }
}

/** Shown in a brand-new conversation. */
@Composable
fun ConversationIntro(peerName: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 48.dp),
    ) {
        MoeAvatar(name = peerName, size = 80.dp, inverted = true)
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.say_hi, peerName.substringBefore(' ')),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(Res.string.say_hi_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.extended.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.width(0.dp))
    }
}
