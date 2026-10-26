package com.laarasoft.moepay.core.designsystem.native

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.icon.painter
import com.laarasoft.moepay.core.designsystem.theme.extended

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun MoeTopBar(
    title: String,
    modifier: Modifier,
    navigationAction: MoeBarAction?,
    actions: List<MoeBarAction>,
    subtitle: String?,
) {
    val colorScheme = MaterialTheme.colorScheme
    TopAppBar(
        title = {
            if (subtitle == null) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
            } else {
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.extended.textSecondary,
                        maxLines = 1,
                    )
                }
            }
        },
        navigationIcon = {
            if (navigationAction != null) {
                IconButton(onClick = navigationAction.onClick) {
                    Icon(
                        painter = navigationAction.icon.painter(),
                        contentDescription = navigationAction.contentDescription,
                    )
                }
            }
        },
        actions = {
            actions.forEach { action ->
                IconButton(onClick = action.onClick) {
                    Icon(painter = action.icon.painter(), contentDescription = action.contentDescription)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorScheme.background,
            scrolledContainerColor = colorScheme.background,
            titleContentColor = colorScheme.onBackground,
            navigationIconContentColor = colorScheme.onBackground,
            actionIconContentColor = colorScheme.onBackground,
        ),
        modifier = modifier,
    )
}

@Composable
actual fun MoeNavigationBar(
    items: List<MoeNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    NavigationBar(
        containerColor = colorScheme.background,
        contentColor = colorScheme.onBackground,
        tonalElevation = NavigationBarDefaultsNoElevation,
        modifier = modifier,
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(index) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (item.badgeCount > 0) {
                                Badge(
                                    containerColor = colorScheme.onBackground,
                                    contentColor = colorScheme.background,
                                ) { Text(if (item.badgeCount > 99) "99+" else item.badgeCount.toString()) }
                            }
                        },
                    ) {
                        Icon(
                            painter = (if (selected) item.selectedIcon else item.icon).painter(),
                            contentDescription = null,
                        )
                    }
                },
                label = { Text(text = item.label, style = MaterialTheme.typography.labelMedium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorScheme.onBackground,
                    selectedTextColor = colorScheme.onBackground,
                    indicatorColor = colorScheme.extended.fieldBackground,
                    unselectedIconColor = colorScheme.extended.textTertiary,
                    unselectedTextColor = colorScheme.extended.textTertiary,
                ),
            )
        }
    }
}

private val NavigationBarDefaultsNoElevation = androidx.compose.ui.unit.Dp(0f)

@Composable
actual fun MoeAlertDialog(
    title: String,
    message: String?,
    confirmButton: MoeDialogButton,
    onDismissRequest: () -> Unit,
    dismissButton: MoeDialogButton?,
) {
    val colorScheme = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        text = message?.let { { Text(text = it, style = MaterialTheme.typography.bodyMedium) } },
        confirmButton = {
            TextButton(onClick = confirmButton.onClick) {
                Text(
                    text = confirmButton.text,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (confirmButton.style == MoeDialogButtonStyle.DESTRUCTIVE) colorScheme.error else colorScheme.onSurface,
                )
            }
        },
        dismissButton = dismissButton?.let { button ->
            {
                TextButton(onClick = button.onClick) {
                    Text(
                        text = button.text,
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.extended.textSecondary,
                    )
                }
            }
        },
        containerColor = colorScheme.surface,
        titleContentColor = colorScheme.onSurface,
        textContentColor = colorScheme.extended.textSecondary,
        shape = MaterialTheme.shapes.large,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun MoeActionSheet(
    actions: List<MoeSheetAction>,
    cancelLabel: String,
    onDismissRequest: () -> Unit,
    title: String?,
) {
    val colorScheme = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = colorScheme.surface,
    ) {
        Column(Modifier.padding(bottom = 16.dp)) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.extended.textSecondary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
            }
            actions.forEach { action ->
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (action.style == MoeDialogButtonStyle.DESTRUCTIVE) colorScheme.error else colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            action.onClick()
                            onDismissRequest()
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                )
            }
            Text(
                text = cancelLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.extended.textSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDismissRequest)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            )
        }
    }
}
