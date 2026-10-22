package com.laarasoft.moepay.core.designsystem.native

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

/** Top app bar. Handles the status bar inset itself. */
@Composable
expect fun MoeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationAction: MoeBarAction? = null,
    actions: List<MoeBarAction> = emptyList(),
)

/** Bottom tab bar. Handles the navigation bar / home indicator inset itself. */
@Composable
expect fun MoeNavigationBar(
    items: List<MoeNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
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
