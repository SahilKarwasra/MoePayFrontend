package com.laarasoft.moepay.core.designsystem.native

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.laarasoft.moepay.core.designsystem.icon.painter
import com.laarasoft.moepay.core.designsystem.theme.extended

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun MoeTopBar(
    title: String,
    modifier: Modifier,
    navigationAction: MoeBarAction?,
    actions: List<MoeBarAction>,
) {
    val colorScheme = MaterialTheme.colorScheme
    TopAppBar(
        title = {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
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
                    Icon(
                        painter = (if (selected) item.selectedIcon else item.icon).painter(),
                        contentDescription = null,
                    )
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
