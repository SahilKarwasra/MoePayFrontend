package com.laarasoft.moepay.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeButtonStyle
import com.laarasoft.moepay.core.designsystem.components.MoePinPad
import com.laarasoft.moepay.core.designsystem.native.MoeAlertDialog
import com.laarasoft.moepay.core.designsystem.native.MoeDialogButton
import com.laarasoft.moepay.core.designsystem.native.MoeDialogButtonStyle
import com.laarasoft.moepay.core.designsystem.native.MoeSheet
import com.laarasoft.moepay.core.designsystem.native.MoeSheetSize
import com.laarasoft.moepay.core.presentation.util.UiText
import com.laarasoft.moepay.core.ui.resources.Res
import com.laarasoft.moepay.core.ui.resources.enter_pin
import com.laarasoft.moepay.core.ui.resources.forgot_pin
import com.laarasoft.moepay.core.ui.resources.not_now
import com.laarasoft.moepay.core.ui.resources.pin_locked_title
import com.laarasoft.moepay.core.ui.resources.reset_pin
import org.jetbrains.compose.resources.stringResource

/**
 * The PIN step of every payment, in a native sheet. The PIN goes straight to [onPinEntered]
 * and is never kept in screen state.
 */
@Composable
fun PinEntrySheet(
    visible: Boolean,
    subtitle: String?,
    error: UiText?,
    errorKey: Int,
    isLoading: Boolean,
    onPinEntered: (String) -> Unit,
    onForgotPin: () -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(Res.string.enter_pin),
) {
    MoeSheet(visible = visible, onDismissRequest = onDismiss, size = MoeSheetSize.LARGE) {
        MoePinPad(
            title = title,
            subtitle = subtitle,
            errorText = error?.asString(),
            errorKey = errorKey,
            isLoading = isLoading,
            onComplete = onPinEntered,
            modifier = Modifier.padding(top = 12.dp),
            footer = {
                MoeButton(
                    text = stringResource(Res.string.forgot_pin),
                    onClick = onForgotPin,
                    style = MoeButtonStyle.TEXT,
                    enabled = !isLoading,
                    modifier = Modifier.padding(top = 4.dp),
                )
            },
        )
    }
}

/** Shown when too many wrong PINs lock payments; offers the reset flow. */
@Composable
fun PinLockedDialog(
    message: UiText,
    onResetPin: () -> Unit,
    onDismiss: () -> Unit,
) {
    MoeAlertDialog(
        title = stringResource(Res.string.pin_locked_title),
        message = message.asString(),
        confirmButton = MoeDialogButton(text = stringResource(Res.string.reset_pin), onClick = onResetPin),
        dismissButton = MoeDialogButton(text = stringResource(Res.string.not_now), onClick = onDismiss, style = MoeDialogButtonStyle.CANCEL),
        onDismissRequest = onDismiss,
    )
}
