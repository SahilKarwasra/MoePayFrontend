package com.laarasoft.moepay.core.designsystem.native

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class MoeSheetSize {
    /** Height follows the content (e.g. a short confirmation). */
    FIT,

    /** Nearly full height (e.g. the PIN pad). */
    LARGE,
}

/**
 * A modal sheet. iOS presents a real `UISheetPresentationController` (grabber, detents,
 * swipe to dismiss); Android uses a Material bottom sheet.
 *
 * On iOS the content runs in its own composition, so pass state in and events out through
 * parameters; don't read ViewModels from inside [content].
 */
@Composable
expect fun MoeSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    size: MoeSheetSize = MoeSheetSize.FIT,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
)

/** Platform haptics: Taptic Engine on iOS, `HapticFeedbackConstants` on Android. */
interface MoeHaptics {
    fun tap()
    fun selection()
    fun success()
    fun error()
}

@Composable
expect fun rememberHaptics(): MoeHaptics

/** Opens the system share sheet with [text]. */
@Composable
expect fun rememberShareSheet(): (text: String) -> Unit

/** Copies [text] to the system clipboard. */
@Composable
expect fun rememberClipboard(): (text: String) -> Unit

/**
 * Native QR scanner: VisionKit's scanner on iOS (registered from Swift), Google Code Scanner
 * on Android. [isAvailable] is false on simulators and devices without a supported camera.
 */
interface MoeQrScanner {
    val isAvailable: Boolean
    fun scan()
}

@Composable
expect fun rememberQrScanner(onResult: (payload: String?) -> Unit): MoeQrScanner

/** `UISegmentedControl` on iOS, Material segmented buttons on Android. */
@Composable
expect fun MoeSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
)

/** `UISwitch` on iOS, Material `Switch` on Android. */
@Composable
expect fun MoeSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
)
