package com.laarasoft.moepay.chat.presentation.conversation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.chat.domain.ChatPolicy
import com.laarasoft.moepay.chat.presentation.components.ChatIconButton
import com.laarasoft.moepay.chat.presentation.resources.Res
import com.laarasoft.moepay.chat.presentation.resources.attach_money
import com.laarasoft.moepay.chat.presentation.resources.cancel
import com.laarasoft.moepay.chat.presentation.resources.message_placeholder
import com.laarasoft.moepay.chat.presentation.resources.send
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.native.rememberHaptics
import com.laarasoft.moepay.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource

/** What the composer is attached to: a reply or an edit. */
data class ComposerContext(
    val title: String,
    val preview: String,
    val icon: MoeIcon,
)

@Composable
fun Composer(
    state: TextFieldState,
    context: ComposerContext?,
    isEditing: Boolean,
    onSend: () -> Unit,
    onMoney: () -> Unit,
    onCancelContext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val haptics = rememberHaptics()
    val canSend = state.text.isNotBlank()

    Column(modifier = modifier.fillMaxWidth().background(colors.background)) {
        AnimatedVisibility(
            visible = context != null,
            enter = expandVertically(tween(200)) + fadeIn(tween(200)),
            exit = shrinkVertically(tween(160)) + fadeOut(tween(120)),
        ) {
            if (context != null) ContextBanner(context, onCancelContext)
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 8.dp),
        ) {
            AnimatedVisibility(visible = !isEditing, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
                ChatIconButton(
                    icon = MoeIcon.Add,
                    contentDescription = stringResource(Res.string.attach_money),
                    onClick = {
                        haptics.tap()
                        onMoney()
                    },
                    size = 38.dp,
                )
            }
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 38.dp)
                    .background(colors.background, RoundedCornerShape(19.dp))
                    .border(1.dp, colors.extended.border, RoundedCornerShape(19.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                if (state.text.isEmpty()) {
                    Text(stringResource(Res.string.message_placeholder), style = MaterialTheme.typography.bodyLarge, color = colors.extended.textTertiary)
                }
                BasicTextField(
                    state = state,
                    lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 1, maxHeightInLines = 6),
                    inputTransformation = androidx.compose.foundation.text.input.InputTransformation.maxLength(ChatPolicy.MAX_MESSAGE_LENGTH),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onBackground),
                    cursorBrush = SolidColor(colors.onBackground),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            AnimatedVisibility(
                visible = canSend,
                enter = scaleIn(tween(180), initialScale = 0.4f) + fadeIn(tween(150)),
                exit = scaleOut(tween(140), targetScale = 0.4f) + fadeOut(tween(120)),
            ) {
                ChatIconButton(
                    icon = if (isEditing) MoeIcon.Check else MoeIcon.ArrowUp,
                    contentDescription = stringResource(Res.string.send),
                    onClick = {
                        haptics.tap()
                        onSend()
                    },
                    size = 38.dp,
                    inverted = true,
                )
            }
        }
    }
}

@Composable
private fun ContextBanner(context: ComposerContext, onCancel: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 10.dp, top = 8.dp),
    ) {
        Box(Modifier.width(3.dp).height(34.dp).background(colors.onBackground, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(context.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = colors.onBackground, maxLines = 1)
            Text(
                context.preview,
                style = MaterialTheme.typography.bodySmall,
                color = colors.extended.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ChatIconButton(icon = MoeIcon.Close, contentDescription = stringResource(Res.string.cancel), onClick = onCancel, size = 30.dp)
    }
}
