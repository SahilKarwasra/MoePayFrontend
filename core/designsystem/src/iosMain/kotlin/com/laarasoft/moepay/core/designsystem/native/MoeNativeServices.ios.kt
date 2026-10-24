package com.laarasoft.moepay.core.designsystem.native

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIAction
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIControlStateSelected
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.UIKit.UIPasteboard
import platform.UIKit.UISegmentedControl
import platform.UIKit.UISelectionFeedbackGenerator
import platform.UIKit.UISwitch
import platform.UIKit.popoverPresentationController

@Composable
actual fun rememberHaptics(): MoeHaptics = remember {
    object : MoeHaptics {
        override fun tap() {
            UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight).impactOccurred()
        }

        override fun selection() {
            UISelectionFeedbackGenerator().selectionChanged()
        }

        override fun success() {
            UINotificationFeedbackGenerator().notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
        }

        override fun error() {
            UINotificationFeedbackGenerator().notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
        }
    }
}

@Composable
actual fun rememberShareSheet(): (text: String) -> Unit {
    val viewController = LocalUIViewController.current
    return remember(viewController) {
        { text ->
            val activity = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
            val presenter = viewController.topMostPresented()
            activity.popoverPresentationController?.sourceView = presenter.view
            presenter.presentViewController(activity, animated = true, completion = null)
        }
    }
}

@Composable
actual fun rememberClipboard(): (text: String) -> Unit = remember {
    { text -> UIPasteboard.generalPasteboard.string = text }
}

/** Implemented in Swift (VisionKit's scanner is a Swift-only API) and registered at launch. */
interface IosQrScanner {
    val isAvailable: Boolean
    fun scan(onResult: (String?) -> Unit)
}

object IosQrScannerRegistry {
    var scanner: IosQrScanner? = null
}

@Composable
actual fun rememberQrScanner(onResult: (payload: String?) -> Unit): MoeQrScanner {
    val currentOnResult by rememberUpdatedState(onResult)
    return remember {
        object : MoeQrScanner {
            override val isAvailable: Boolean
                get() = IosQrScannerRegistry.scanner?.isAvailable == true

            override fun scan() {
                val scanner = IosQrScannerRegistry.scanner
                if (scanner == null || !scanner.isAvailable) {
                    currentOnResult(null)
                    return
                }
                scanner.scan { currentOnResult(it) }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun MoeSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val currentOnSelect by rememberUpdatedState(onSelect)
    val selectedTint = colorScheme.onBackground.toUIColor()
    val selectedText = colorScheme.background.toUIColor()
    val normalText = colorScheme.onBackground.toUIColor()

    UIKitView(
        factory = {
            UISegmentedControl(items = options).apply {
                addAction(
                    UIAction.actionWithHandler { _ -> currentOnSelect(selectedSegmentIndex.toInt()) },
                    forControlEvents = UIControlEventValueChanged,
                )
            }
        },
        update = { control ->
            if (control.numberOfSegments.toInt() != options.size) {
                control.removeAllSegments()
                options.forEachIndexed { index, title ->
                    control.insertSegmentWithTitle(title, atIndex = index.toULong(), animated = false)
                }
            } else {
                options.forEachIndexed { index, title -> control.setTitle(title, forSegmentAtIndex = index.toULong()) }
            }
            control.selectedSegmentIndex = selectedIndex.toLong()
            control.selectedSegmentTintColor = selectedTint
            control.setTitleTextAttributes(mapOf<Any?, Any?>(NSForegroundColorAttributeName to selectedText), forState = UIControlStateSelected)
            control.setTitleTextAttributes(mapOf<Any?, Any?>(NSForegroundColorAttributeName to normalText), forState = UIControlStateNormal)
        },
        modifier = modifier.height(34.dp),
        properties = UIKitInteropProperties(isNativeAccessibilityEnabled = true),
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun MoeSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
) {
    val currentOnChange by rememberUpdatedState(onCheckedChange)
    val tint = MaterialTheme.colorScheme.onBackground.toUIColor()
    UIKitView(
        factory = {
            UISwitch().apply {
                addAction(
                    UIAction.actionWithHandler { _ -> currentOnChange(isOn()) },
                    forControlEvents = UIControlEventValueChanged,
                )
            }
        },
        update = { switch ->
            if (switch.isOn() != checked) switch.setOn(checked, animated = true)
            switch.enabled = enabled
            switch.onTintColor = tint
        },
        modifier = modifier.size(width = 51.dp, height = 31.dp),
        properties = UIKitInteropProperties(isNativeAccessibilityEnabled = true),
    )
}
