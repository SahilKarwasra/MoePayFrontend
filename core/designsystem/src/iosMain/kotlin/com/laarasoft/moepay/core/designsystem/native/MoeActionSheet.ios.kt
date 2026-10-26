package com.laarasoft.moepay.core.designsystem.native

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleCancel
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertActionStyleDestructive
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleActionSheet
import platform.UIKit.popoverPresentationController

/** Presents a native action sheet while this composable is in composition. */
@Composable
actual fun MoeActionSheet(
    actions: List<MoeSheetAction>,
    cancelLabel: String,
    onDismissRequest: () -> Unit,
    title: String?,
) {
    val viewController = LocalUIViewController.current
    val currentActions by rememberUpdatedState(actions)
    val currentDismiss by rememberUpdatedState(onDismissRequest)

    DisposableEffect(title, actions.map { it.title }) {
        var handled = false
        val sheet = UIAlertController.alertControllerWithTitle(
            title = title,
            message = null,
            preferredStyle = UIAlertControllerStyleActionSheet,
        )
        actions.forEachIndexed { index, action ->
            sheet.addAction(
                UIAlertAction.actionWithTitle(
                    title = action.title,
                    style = if (action.style == MoeDialogButtonStyle.DESTRUCTIVE) UIAlertActionStyleDestructive else UIAlertActionStyleDefault,
                    handler = { _ ->
                        handled = true
                        currentActions.getOrNull(index)?.onClick?.invoke()
                        currentDismiss()
                    },
                )
            )
        }
        sheet.addAction(
            UIAlertAction.actionWithTitle(
                title = cancelLabel,
                style = UIAlertActionStyleCancel,
                handler = { _ ->
                    handled = true
                    currentDismiss()
                },
            )
        )
        val presenter = viewController.topMostPresented()
        // iPad presents action sheets as popovers, which need an anchor.
        sheet.popoverPresentationController?.sourceView = presenter.view
        presenter.presentViewController(sheet, animated = true, completion = null)

        onDispose {
            if (!handled) sheet.dismissViewControllerAnimated(true, completion = null)
        }
    }
}
