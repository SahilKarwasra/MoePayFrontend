package com.laarasoft.moepay.core.designsystem.native

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyle
import platform.UIKit.UIAlertActionStyleCancel
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertActionStyleDestructive
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIViewController

/** Presents a native UIAlertController while this composable is in composition. */
@Composable
actual fun MoeAlertDialog(
    title: String,
    message: String?,
    confirmButton: MoeDialogButton,
    onDismissRequest: () -> Unit,
    dismissButton: MoeDialogButton?,
) {
    val viewController = LocalUIViewController.current
    val currentConfirm by rememberUpdatedState(confirmButton)
    val currentDismiss by rememberUpdatedState(dismissButton)

    DisposableEffect(title, message, confirmButton.text, dismissButton?.text) {
        var handledByUser = false
        val alert = UIAlertController.alertControllerWithTitle(
            title = title,
            message = message,
            preferredStyle = UIAlertControllerStyleAlert,
        )
        if (dismissButton != null) {
            alert.addAction(
                UIAlertAction.actionWithTitle(
                    title = dismissButton.text,
                    style = dismissButton.style.toUIKit(fallback = UIAlertActionStyleCancel),
                    handler = { _ ->
                        handledByUser = true
                        currentDismiss?.onClick?.invoke()
                    },
                )
            )
        }
        alert.addAction(
            UIAlertAction.actionWithTitle(
                title = confirmButton.text,
                style = confirmButton.style.toUIKit(fallback = UIAlertActionStyleDefault),
                handler = { _ ->
                    handledByUser = true
                    currentConfirm.onClick()
                },
            )
        )
        viewController.topMostPresented().presentViewController(alert, animated = true, completion = null)

        onDispose {
            if (!handledByUser) {
                alert.dismissViewControllerAnimated(true, completion = null)
            }
        }
    }
}

private fun MoeDialogButtonStyle.toUIKit(fallback: UIAlertActionStyle): UIAlertActionStyle = when (this) {
    MoeDialogButtonStyle.DEFAULT -> fallback
    MoeDialogButtonStyle.CANCEL -> UIAlertActionStyleCancel
    MoeDialogButtonStyle.DESTRUCTIVE -> UIAlertActionStyleDestructive
}

private fun UIViewController.topMostPresented(): UIViewController {
    var current: UIViewController = this
    while (true) {
        current = current.presentedViewController ?: return current
    }
}
