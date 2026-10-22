package com.laarasoft.moepay.onboarding.presentation.profile

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.laarasoft.moepay.core.designsystem.theme.MoeTheme
import com.laarasoft.moepay.core.domain.onboarding.OnboardingStep
import com.laarasoft.moepay.core.presentation.util.ObserveAsEvents
import com.laarasoft.moepay.onboarding.domain.ProfileValidator
import com.laarasoft.moepay.onboarding.presentation.components.OnboardingTopBar
import com.laarasoft.moepay.onboarding.presentation.resources.Res
import com.laarasoft.moepay.onboarding.presentation.resources.continue_button
import com.laarasoft.moepay.onboarding.presentation.resources.profile_email
import com.laarasoft.moepay.onboarding.presentation.resources.profile_email_invalid
import com.laarasoft.moepay.onboarding.presentation.resources.profile_email_placeholder
import com.laarasoft.moepay.onboarding.presentation.resources.profile_first_name
import com.laarasoft.moepay.onboarding.presentation.resources.profile_first_name_placeholder
import com.laarasoft.moepay.onboarding.presentation.resources.profile_last_name
import com.laarasoft.moepay.onboarding.presentation.resources.profile_last_name_placeholder
import com.laarasoft.moepay.onboarding.presentation.resources.profile_subtitle
import com.laarasoft.moepay.onboarding.presentation.resources.profile_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileRoot(
    canGoBack: Boolean,
    onBack: () -> Unit,
    onNextStep: (OnboardingStep) -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ProfileEvent.NextStep -> onNextStep(event.step)
        }
    }

    ProfileScreen(
        state = state,
        canGoBack = canGoBack,
        onAction = { action ->
            when (action) {
                ProfileAction.OnBackClick -> onBack()
                else -> Unit
            }
            viewModel.onAction(action)
        },
    )
}

@Composable
fun ProfileScreen(
    state: ProfileState,
    canGoBack: Boolean,
    onAction: (ProfileAction) -> Unit,
) {
    val nameTransformation = InputTransformation.maxLength(ProfileValidator.NAME_MAX_LENGTH)
    val nameKeyboard = KeyboardOptions(
        capitalization = KeyboardCapitalization.Words,
        imeAction = ImeAction.Next,
    )

    MoeFormLayout(
        topBar = {
            OnboardingTopBar(
                step = 1,
                canGoBack = canGoBack,
                onBack = { onAction(ProfileAction.OnBackClick) },
            )
        },
        bottomContent = {
            MoeButton(
                text = stringResource(Res.string.continue_button),
                onClick = { onAction(ProfileAction.OnContinueClick) },
                enabled = state.canContinue,
                isLoading = state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        MoeScreenHeader(
            title = stringResource(Res.string.profile_title),
            subtitle = stringResource(Res.string.profile_subtitle),
        )
        Spacer(Modifier.height(28.dp))
        MoeTextField(
            state = state.firstNameState,
            title = stringResource(Res.string.profile_first_name),
            placeholder = stringResource(Res.string.profile_first_name_placeholder),
            inputTransformation = nameTransformation,
            keyboardOptions = nameKeyboard,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        MoeTextField(
            state = state.lastNameState,
            title = stringResource(Res.string.profile_last_name),
            placeholder = stringResource(Res.string.profile_last_name_placeholder),
            inputTransformation = nameTransformation,
            keyboardOptions = nameKeyboard,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        MoeTextField(
            state = state.emailState,
            title = stringResource(Res.string.profile_email),
            placeholder = stringResource(Res.string.profile_email_placeholder),
            isError = !state.isEmailValid,
            supportingText = if (!state.isEmailValid) stringResource(Res.string.profile_email_invalid) else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
            onKeyboardAction = { onAction(ProfileAction.OnContinueClick) },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        )
        state.error?.let { error ->
            Spacer(Modifier.height(16.dp))
            Text(
                text = error.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    MoeTheme { ProfileScreen(state = ProfileState(), canGoBack = false, onAction = {}) }
}
