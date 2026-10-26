package com.laarasoft.moepay.core.designsystem.native

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon

/*
 * Chrome that should feel native on each platform.
 * iOS renders real UIKit controls (UINavigationBar, UITabBar, UIAlertController);
 * Android renders their Material 3 counterparts.
 */

data class MoeBarAction(
    val icon: MoeIcon,
    val contentDescription: String,
    val onClick: () -> Unit,
)

data class MoeNavigationItem(
    val label: String,
    val icon: MoeIcon,
    val selectedIcon: MoeIcon = icon,
    /** Shown as a native badge (e.g. unread count); 0 hides it. */
    val badgeCount: Int = 0,
)

enum class MoeDialogButtonStyle {
    DEFAULT,
    CANCEL,
    DESTRUCTIVE,
}

data class MoeDialogButton(
    val text: String,
    val onClick: () -> Unit,
    val style: MoeDialogButtonStyle = MoeDialogButtonStyle.DEFAULT,
)

/**
 * Top app bar. Handles the status bar inset itself. [subtitle] shows under the title
 * (iOS 26's native navigation subtitle; a second line on Android).
 */
@Composable
expect fun MoeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationAction: MoeBarAction? = null,
    actions: List<MoeBarAction> = emptyList(),
    subtitle: String? = null,
)

/**
 * Height of the tab bar floating over tab content (bar + home indicator). Scrollable tab
 * content adds it to its bottom padding so the last item can scroll clear of the bar.
 */
val LocalBottomBarInset = compositionLocalOf { 0.dp }

/**
 * Bottom tab bar, meant to be overlaid at the bottom of the tab content. Handles the
 * navigation bar / home indicator inset itself. On iOS it's the floating glass capsule with
 * content showing around it; on Android an opaque Material bar.
 */
@Composable
expect fun MoeNavigationBar(
    items: List<MoeNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
)

data class MoeSheetAction(
    val title: String,
    val onClick: () -> Unit,
    val style: MoeDialogButtonStyle = MoeDialogButtonStyle.DEFAULT,
)

/**
 * A list of actions, e.g. a message's long-press menu: a native `UIAlertController` action
 * sheet on iOS, a Material bottom sheet on Android. Shown while in composition; a Cancel
 * button is added for you.
 */
@Composable
expect fun MoeActionSheet(
    actions: List<MoeSheetAction>,
    cancelLabel: String,
    onDismissRequest: () -> Unit,
    title: String? = null,
)

/** Shown while in composition; remove it from composition to dismiss. */
@Composable
expect fun MoeAlertDialog(
    title: String,
    message: String?,
    confirmButton: MoeDialogButton,
    onDismissRequest: () -> Unit,
    dismissButton: MoeDialogButton? = null,
)
