package com.laarasoft.moepay.onboarding.presentation.username

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeFormLayout
import com.laarasoft.moepay.core.designsystem.components.MoeScreenHeader
import com.laarasoft.moepay.core.designsystem.components.MoeTextField
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.icon.painter
import com.laarasoft.moepay.core.designsystem.theme.MoeTheme
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.domain.onboarding.OnboardingStep
import com.laarasoft.moepay.core.presentation.util.ObserveAsEvents
import com.laarasoft.moepay.onboarding.domain.UsernameValidator
import com.laarasoft.moepay.onboarding.presentation.components.OnboardingTopBar
import com.laarasoft.moepay.onboarding.presentation.resources.Res
import com.laarasoft.moepay.onboarding.presentation.resources.continue_button
import com.laarasoft.moepay.onboarding.presentation.resources.username_available
import com.laarasoft.moepay.onboarding.presentation.resources.username_checking
import com.laarasoft.moepay.onboarding.presentation.resources.username_field
import com.laarasoft.moepay.onboarding.presentation.resources.username_invalid
import com.laarasoft.moepay.onboarding.presentation.resources.username_placeholder
import com.laarasoft.moepay.onboarding.presentation.resources.username_reserved
import com.laarasoft.moepay.onboarding.presentation.resources.username_rules
import com.laarasoft.moepay.onboarding.presentation.resources.username_subtitle
import com.laarasoft.moepay.onboarding.presentation.resources.username_taken
import com.laarasoft.moepay.onboarding.presentation.resources.username_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UsernameRoot(
    canGoBack: Boolean,
    onBack: () -> Unit,
    onNextStep: (OnboardingStep) -> Unit,
    viewModel: UsernameViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is UsernameEvent.NextStep -> onNextStep(event.step)
        }
    }

    UsernameScreen(
        state = state,
        canGoBack = canGoBack,
        onAction = { action ->
            when (action) {
                UsernameAction.OnBackClick -> onBack()
                else -> Unit
            }
            viewModel.onAction(action)
        },
    )
}

@Composable
fun UsernameScreen(
    state: UsernameState,
    canGoBack: Boolean,
    onAction: (UsernameAction) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    MoeFormLayout(
        topBar = {
            OnboardingTopBar(
                step = 2,
                canGoBack = canGoBack,
                onBack = { onAction(UsernameAction.OnBackClick) },
            )
        },
        bottomContent = {
            MoeButton(
                text = stringResource(Res.string.continue_button),
                onClick = { onAction(UsernameAction.OnContinueClick) },
                enabled = state.canContinue,
                isLoading = state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        MoeScreenHeader(
            title = stringResource(Res.string.username_title),
            subtitle = stringResource(Res.string.username_subtitle),
        )
        Spacer(Modifier.height(28.dp))
        MoeTextField(
            state = state.usernameState,
            title = stringResource(Res.string.username_field),
            placeholder = stringResource(Res.string.username_placeholder),
            isError = state.status in setOf(UsernameStatus.TAKEN, UsernameStatus.RESERVED, UsernameStatus.INVALID),
            inputTransformation = UsernameInputTransformation,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done,
            ),
            onKeyboardAction = { onAction(UsernameAction.OnContinueClick) },
            enabled = !state.isSaving,
            leadingContent = {
                Text(
                    text = "@",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.extended.textTertiary,
                )
            },
            trailingContent = { StatusIcon(state.status) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )
        Spacer(Modifier.height(10.dp))
        StatusLine(
            status = state.status,
            username = UsernameValidator.normalize(state.usernameState.text.toString()),
            errorText = state.error?.asString(),
        )
    }
}

@Composable
private fun StatusIcon(status: UsernameStatus) {
    when (status) {
        UsernameStatus.CHECKING -> CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onBackground,
        )
        UsernameStatus.AVAILABLE -> Icon(
            painter = MoeIcon.CheckCircle.painter(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(20.dp),
        )
        UsernameStatus.TAKEN,
        UsernameStatus.RESERVED,
        UsernameStatus.INVALID -> Icon(
            painter = MoeIcon.CloseCircle.painter(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp),
        )
        UsernameStatus.IDLE -> Unit
    }
}

@Composable
private fun StatusLine(
    status: UsernameStatus,
    username: String,
    errorText: String?,
) {
    AnimatedContent(
        targetState = status to errorText,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "usernameStatus",
    ) { (currentStatus, error) ->
        val colorScheme = MaterialTheme.colorScheme
        val (text, color) = when {
            error != null -> error to colorScheme.error
            currentStatus == UsernameStatus.CHECKING -> stringResource(Res.string.username_checking) to colorScheme.extended.textTertiary
            currentStatus == UsernameStatus.AVAILABLE -> stringResource(Res.string.username_available, username) to colorScheme.onBackground
            currentStatus == UsernameStatus.TAKEN -> stringResource(Res.string.username_taken) to colorScheme.error
            currentStatus == UsernameStatus.RESERVED -> stringResource(Res.string.username_reserved) to colorScheme.error
            currentStatus == UsernameStatus.INVALID -> stringResource(Res.string.username_invalid) to colorScheme.error
            else -> stringResource(Res.string.username_rules) to colorScheme.extended.textSecondary
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = text, style = MaterialTheme.typography.bodySmall, color = color)
        }
    }
}

@Preview
@Composable
private fun UsernameScreenPreview() {
    MoeTheme {
        UsernameScreen(state = UsernameState(status = UsernameStatus.AVAILABLE), canGoBack = true, onAction = {})
    }
}
