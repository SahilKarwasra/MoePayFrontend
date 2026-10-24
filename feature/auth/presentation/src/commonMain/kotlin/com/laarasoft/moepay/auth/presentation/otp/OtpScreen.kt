package com.laarasoft.moepay.auth.presentation.otp

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.auth.domain.OtpValidator
import com.laarasoft.moepay.auth.presentation.resources.Res
import com.laarasoft.moepay.auth.presentation.resources.back
import com.laarasoft.moepay.auth.presentation.resources.otp_resend
import com.laarasoft.moepay.auth.presentation.resources.otp_resend_in
import com.laarasoft.moepay.auth.presentation.resources.otp_subtitle
import com.laarasoft.moepay.auth.presentation.resources.otp_title
import com.laarasoft.moepay.auth.presentation.resources.otp_verify
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeButtonStyle
import com.laarasoft.moepay.core.designsystem.components.MoeFormLayout
import com.laarasoft.moepay.core.designsystem.components.MoeOtpField
import com.laarasoft.moepay.core.designsystem.components.MoeScreenHeader
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.native.MoeBarAction
import com.laarasoft.moepay.core.designsystem.native.MoeTopBar
import com.laarasoft.moepay.core.designsystem.theme.MoeTheme
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.domain.onboarding.OnboardingStep
import com.laarasoft.moepay.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun OtpRoot(
    phoneNumber: String,
    resendAfterSeconds: Int,
    onBack: () -> Unit,
    onVerified: (OnboardingStep) -> Unit,
    viewModel: OtpViewModel = koinViewModel { parametersOf(phoneNumber, resendAfterSeconds) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is OtpEvent.Verified -> onVerified(event.nextStep)
        }
    }

    OtpScreen(
        state = state,
        onAction = { action ->
            when (action) {
                OtpAction.OnBackClick -> onBack()
                else -> Unit
            }
            viewModel.onAction(action)
        },
    )
}

@Composable
fun OtpScreen(
    state: OtpState,
    onAction: (OtpAction) -> Unit,
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
                    onClick = { onAction(OtpAction.OnBackClick) },
                ),
            )
        },
        bottomContent = {
            MoeButton(
                text = stringResource(Res.string.otp_verify),
                onClick = { onAction(OtpAction.OnVerifyClick) },
                enabled = state.canVerify,
                isLoading = state.isVerifying,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        MoeScreenHeader(
            title = stringResource(Res.string.otp_title),
            subtitle = stringResource(Res.string.otp_subtitle, state.formattedPhoneNumber),
        )
        Spacer(Modifier.height(32.dp))
        MoeOtpField(
            state = state.otpState,
            length = OtpValidator.LENGTH,
            isError = state.error != null,
            enabled = !state.isVerifying,
            focusRequester = focusRequester,
        )
        Spacer(Modifier.height(16.dp))

        val message = state.error ?: state.info
        if (message != null) {
            Text(
                text = message.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = if (state.error != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.extended.textSecondary
                },
            )
            Spacer(Modifier.height(8.dp))
        }

        AnimatedContent(
            targetState = state.resendSecondsLeft > 0,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "resend",
        ) { isCountingDown ->
            if (isCountingDown) {
                Text(
                    text = stringResource(Res.string.otp_resend_in, state.resendSecondsLeft.asCountdown()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.extended.textTertiary,
                )
            } else {
                MoeButton(
                    text = stringResource(Res.string.otp_resend),
                    onClick = { onAction(OtpAction.OnResendClick) },
                    style = MoeButtonStyle.TEXT,
                    enabled = state.canResend,
                    isLoading = state.isResending,
                )
            }
        }
    }
}

private fun Int.asCountdown(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

@Preview
@Composable
private fun OtpScreenPreview() {
    MoeTheme {
        OtpScreen(
            state = OtpState(formattedPhoneNumber = "+44 7911 123456", resendSecondsLeft = 27),
            onAction = {},
        )
    }
}
