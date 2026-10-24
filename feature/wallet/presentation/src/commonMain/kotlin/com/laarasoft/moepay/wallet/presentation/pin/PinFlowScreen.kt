package com.laarasoft.moepay.wallet.presentation.pin

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeIconTile
import com.laarasoft.moepay.core.designsystem.components.MoeStepIndicator
import com.laarasoft.moepay.core.designsystem.components.MoeButtonStyle
import com.laarasoft.moepay.core.designsystem.components.MoeOtpField
import com.laarasoft.moepay.core.designsystem.components.MoePinPad
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.native.MoeBarAction
import com.laarasoft.moepay.core.designsystem.native.MoeTopBar
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.presentation.util.ObserveAsEvents
import com.laarasoft.moepay.core.ui.MoneyResultContent
import com.laarasoft.moepay.core.ui.PinLockedDialog
import com.laarasoft.moepay.wallet.presentation.resources.Res
import com.laarasoft.moepay.wallet.presentation.resources.back
import com.laarasoft.moepay.wallet.presentation.resources.pin_changed_done
import com.laarasoft.moepay.wallet.presentation.resources.pin_confirm_subtitle
import com.laarasoft.moepay.wallet.presentation.resources.pin_confirm_title
import com.laarasoft.moepay.wallet.presentation.resources.pin_create_subtitle
import com.laarasoft.moepay.wallet.presentation.resources.pin_create_title
import com.laarasoft.moepay.wallet.presentation.resources.pin_current_subtitle
import com.laarasoft.moepay.wallet.presentation.resources.pin_current_title
import com.laarasoft.moepay.wallet.presentation.resources.pin_hint
import com.laarasoft.moepay.wallet.presentation.resources.pin_done_message
import com.laarasoft.moepay.wallet.presentation.resources.pin_new_title
import com.laarasoft.moepay.wallet.presentation.resources.pin_otp_resend
import com.laarasoft.moepay.wallet.presentation.resources.pin_otp_resend_in
import com.laarasoft.moepay.wallet.presentation.resources.pin_otp_subtitle
import com.laarasoft.moepay.wallet.presentation.resources.pin_otp_title
import com.laarasoft.moepay.wallet.presentation.resources.pin_reset_done
import com.laarasoft.moepay.wallet.presentation.resources.pin_set_done
import com.laarasoft.moepay.wallet.presentation.resources.sending_code
import com.laarasoft.moepay.wallet.presentation.resources.title_change_pin
import com.laarasoft.moepay.wallet.presentation.resources.title_reset_pin
import com.laarasoft.moepay.wallet.presentation.resources.title_set_pin
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PinFlowRoot(
    mode: PinMode,
    onFinished: () -> Unit,
    onOpenReset: () -> Unit,
    viewModel: PinFlowViewModel = koinViewModel(key = mode.name) { parametersOf(mode) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            PinFlowEvent.Finished -> onFinished()
            PinFlowEvent.OpenReset -> onOpenReset()
        }
    }
    PinFlowScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun PinFlowScreen(
    state: PinFlowState,
    onAction: (PinFlowAction) -> Unit,
) {
    if (state.step == PinStep.DONE) {
        MoneyResultContent(
            isSuccess = true,
            headline = stringResource(
                when (state.mode) {
                    PinMode.SET -> Res.string.pin_set_done
                    PinMode.CHANGE -> Res.string.pin_changed_done
                    PinMode.RESET -> Res.string.pin_reset_done
                }
            ),
            message = stringResource(Res.string.pin_done_message),
            details = emptyList(),
            onDone = { onAction(PinFlowAction.OnDone) },
        )
        return
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        MoeTopBar(
            title = stringResource(
                when (state.mode) {
                    PinMode.SET -> Res.string.title_set_pin
                    PinMode.CHANGE -> Res.string.title_change_pin
                    PinMode.RESET -> Res.string.title_reset_pin
                }
            ),
            navigationAction = MoeBarAction(MoeIcon.Back, stringResource(Res.string.back)) { onAction(PinFlowAction.OnBack) },
        )
        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                (slideInHorizontally(tween(260)) { if (forward) it / 3 else -it / 3 } + fadeIn(tween(260))) togetherWith
                    (slideOutHorizontally(tween(220)) { if (forward) -it / 3 else it / 3 } + fadeOut(tween(220)))
            },
            label = "pinStep",
            modifier = Modifier.weight(1f).windowInsetsPadding(WindowInsets.navigationBars),
        ) { step ->
            when (step) {
                PinStep.OTP -> OtpStep(state, onAction)
                else -> MoePinPad(
                    title = stringResource(
                        when (step) {
                            PinStep.CURRENT -> Res.string.pin_current_title
                            PinStep.CONFIRM -> Res.string.pin_confirm_title
                            else -> if (state.mode == PinMode.SET) Res.string.pin_create_title else Res.string.pin_new_title
                        }
                    ),
                    subtitle = stringResource(
                        when (step) {
                            PinStep.CURRENT -> Res.string.pin_current_subtitle
                            PinStep.CONFIRM -> Res.string.pin_confirm_subtitle
                            else -> Res.string.pin_create_subtitle
                        }
                    ),
                    icon = if (step == PinStep.CONFIRM) MoeIcon.CheckCircle else MoeIcon.Lock,
                    step = state.mode.steps.indexOf(step) + 1,
                    totalSteps = state.mode.steps.size,
                    hint = if (step == PinStep.NEW) stringResource(Res.string.pin_hint) else null,
                    errorText = state.error?.asString(),
                    errorKey = state.errorKey,
                    isLoading = state.isLoading,
                    onComplete = { onAction(PinFlowAction.OnPinEntered(it)) },
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
        }
    }

    state.lockedMessage?.let { message ->
        PinLockedDialog(
            message = message,
            onResetPin = { onAction(PinFlowAction.OnResetPinFromLock) },
            onDismiss = { onAction(PinFlowAction.OnDismissLocked) },
        )
    }
}

@Composable
private fun OtpStep(state: PinFlowState, onAction: (PinFlowAction) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        MoeStepIndicator(current = 1, total = state.mode.steps.size, modifier = Modifier.width(72.dp))
        Spacer(Modifier.height(24.dp))
        MoeIconTile(icon = MoeIcon.Lock, size = 64.dp, inverted = true)
        Spacer(Modifier.height(20.dp))
        Text(stringResource(Res.string.pin_otp_title), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (state.otpPhone.isEmpty()) stringResource(Res.string.sending_code) else stringResource(Res.string.pin_otp_subtitle, state.otpPhone),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.extended.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        MoeOtpField(state = state.otpState, isError = state.error != null, focusRequester = focusRequester, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        state.error?.let {
            Text(it.asString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(12.dp))
        if (state.resendSecondsLeft > 0) {
            val left = state.resendSecondsLeft
            Text(
                stringResource(Res.string.pin_otp_resend_in, "${left / 60}:${(left % 60).toString().padStart(2, '0')}"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textTertiary,
            )
        } else {
            MoeButton(
                text = stringResource(Res.string.pin_otp_resend),
                onClick = { onAction(PinFlowAction.OnResendOtp) },
                style = MoeButtonStyle.TEXT,
                isLoading = state.isSendingOtp,
            )
        }
    }
}
