package com.laarasoft.moepay.core.designsystem.native

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import com.laarasoft.moepay.core.designsystem.theme.MoeTheme
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIModalPresentationPageSheet
import platform.UIKit.UIPresentationController
import platform.UIKit.UISheetPresentationControllerDetent
import platform.UIKit.UIViewController
import platform.UIKit.presentationController
import platform.UIKit.sheetPresentationController
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/** Reports swipe-to-dismiss; UIKit keeps delegates weakly, so the effect holds this. */
private class SheetDismissDelegate(private val onDismissed: () -> Unit) :
    NSObject(), UIAdaptivePresentationControllerDelegateProtocol {
    var dismissedByUser = false

    override fun presentationControllerDidDismiss(presentationController: UIPresentationController) {
        dismissedByUser = true
        onDismissed()
    }
}

@Composable
actual fun MoeSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    size: MoeSheetSize,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!visible) return

    val parent = LocalUIViewController.current
    // The sheet has its own composition; these States carry the parent's latest values into it.
    val currentContent = rememberUpdatedState(content)
    val currentOnDismiss = rememberUpdatedState(onDismissRequest)
    val surface = MaterialTheme.colorScheme.surface

    DisposableEffect(Unit) {
        var fittedHeight = 320.0
        val delegate = SheetDismissDelegate { currentOnDismiss.value() }
        lateinit var controller: UIViewController

        controller = ComposeUIViewController {
            MoeTheme(darkTheme = isSystemInDarkTheme()) {
                val density = LocalDensity.current
                Column(
                    modifier = modifier
                        .then(if (size == MoeSheetSize.LARGE) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
                        .background(MaterialTheme.colorScheme.surface)
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
                ) {
                    val sizing = if (size == MoeSheetSize.LARGE) {
                        Modifier.fillMaxSize()
                    } else {
                        // Measured without the sheet's current height as a cap, so the detent can grow
                        // to fit. Detent heights exclude the bottom safe area, as does this Column.
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.Top, unbounded = true)
                            .onSizeChanged { px ->
                                val height = (px.height / density.density).toDouble()
                                if (height > 0 && height != fittedHeight) {
                                    fittedHeight = height
                                    // Resizing the sheet relayouts this view synchronously; doing it
                                    // inside Compose's measure pass crashes, so defer to the next turn.
                                    dispatch_async(dispatch_get_main_queue()) {
                                        controller.sheetPresentationController?.animateChanges {
                                            controller.sheetPresentationController?.invalidateDetents()
                                        }
                                    }
                                }
                            }
                    }
                    Column(modifier = sizing.padding(top = 20.dp)) {
                        currentContent.value(this)
                    }
                }
            }
        }
        controller.view.backgroundColor = surface.toUIColor()
        controller.modalPresentationStyle = UIModalPresentationPageSheet
        controller.sheetPresentationController?.apply {
            detents = if (size == MoeSheetSize.LARGE) {
                listOf(UISheetPresentationControllerDetent.largeDetent())
            } else {
                listOf(UISheetPresentationControllerDetent.customDetentWithIdentifier(null) { _ -> fittedHeight })
            }
            prefersGrabberVisible = true
            preferredCornerRadius = 28.0
        }
        controller.presentationController?.delegate = delegate
        parent.topMostPresented().presentViewController(controller, animated = true, completion = null)

        onDispose {
            if (!delegate.dismissedByUser) {
                controller.dismissViewControllerAnimated(true, completion = null)
            }
        }
    }
}

internal fun UIViewController.topMostPresented(): UIViewController {
    var current: UIViewController = this
    while (true) {
        val next = current.presentedViewController ?: return current
        if (next.isBeingDismissed()) return current
        current = next
    }
}
