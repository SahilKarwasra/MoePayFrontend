package com.laarasoft.moepay.auth.presentation.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.then
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.auth.domain.PhoneNumberValidator
import com.laarasoft.moepay.auth.presentation.resources.Res
import com.laarasoft.moepay.auth.presentation.resources.back
import com.laarasoft.moepay.auth.presentation.resources.continue_button
import com.laarasoft.moepay.auth.presentation.resources.phone_field_title
import com.laarasoft.moepay.auth.presentation.resources.phone_hint
import com.laarasoft.moepay.auth.presentation.resources.phone_placeholder
import com.laarasoft.moepay.auth.presentation.resources.phone_subtitle
import com.laarasoft.moepay.auth.presentation.resources.phone_title
import com.laarasoft.moepay.core.designsystem.components.DigitsOnlyTransformation
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeFormLayout
import com.laarasoft.moepay.core.designsystem.components.MoeScreenHeader
import com.laarasoft.moepay.core.designsystem.components.MoeTextField
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.native.MoeBarAction
import com.laarasoft.moepay.core.designsystem.native.MoeTopBar
import com.laarasoft.moepay.core.designsystem.theme.MoeTheme
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PhoneNumberRoot(
    onBack: () -> Unit,
    onOtpSent: (phoneNumber: String, resendAfterSeconds: Int) -> Unit,
    viewModel: PhoneNumberViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is PhoneNumberEvent.OtpSent -> onOtpSent(event.phoneNumber, event.resendAfterSeconds)
        }
    }

    PhoneNumberScreen(
        state = state,
        onAction = { action ->
            when (action) {
                PhoneNumberAction.OnBackClick -> onBack()
                else -> Unit
            }
            viewModel.onAction(action)
        },
    )
}

@Composable
fun PhoneNumberScreen(
    state: PhoneNumberState,
    onAction: (PhoneNumberAction) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    MoeFormLayout(
        topBar = {
            MoeTopBar(
                title = "",
                navigationAction = MoeBarAction(
                    icon = MoeIcon.Back,
                    contentDescription = stringResource(Res.string.back),
                    onClick = { onAction(PhoneNumberAction.OnBackClick) },
                ),
            )
        },
        bottomContent = {
            MoeButton(
                text = stringResource(Res.string.continue_button),
                onClick = { onAction(PhoneNumberAction.OnContinueClick) },
                enabled = state.canContinue,
                isLoading = state.isSendingOtp,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        MoeScreenHeader(
            title = stringResource(Res.string.phone_title),
            subtitle = stringResource(Res.string.phone_subtitle),
        )
        Spacer(Modifier.height(32.dp))
        MoeTextField(
            state = state.phoneNumberState,
            title = stringResource(Res.string.phone_field_title),
            placeholder = stringResource(Res.string.phone_placeholder),
            supportingText = state.error?.asString() ?: stringResource(Res.string.phone_hint),
            isError = state.error != null,
            enabled = !state.isSendingOtp,
            inputTransformation = DigitsOnlyTransformation
                .then(InputTransformation.maxLength(PhoneNumberValidator.MAX_INPUT_LENGTH)),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done,
            ),
            onKeyboardAction = { onAction(PhoneNumberAction.OnContinueClick) },
            leadingContent = { CountryCode() },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )
    }
}

@Composable
private fun CountryCode() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "${PhoneNumberValidator.FLAG}  ${PhoneNumberValidator.COUNTRY_CODE}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.width(12.dp))
        Spacer(
            Modifier
                .width(1.dp)
                .height(24.dp)
                .background(MaterialTheme.colorScheme.extended.border)
        )
    }
}

@Preview
@Composable
private fun PhoneNumberScreenPreview() {
    MoeTheme { PhoneNumberScreen(state = PhoneNumberState(), onAction = {}) }
}
