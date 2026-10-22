package com.laarasoft.moepay.home.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.core.designsystem.components.MoeEmptyState
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.native.MoeAlertDialog
import com.laarasoft.moepay.core.designsystem.native.MoeBarAction
import com.laarasoft.moepay.core.designsystem.native.MoeDialogButton
import com.laarasoft.moepay.core.designsystem.native.MoeDialogButtonStyle
import com.laarasoft.moepay.core.designsystem.native.MoeNavigationBar
import com.laarasoft.moepay.core.designsystem.native.MoeNavigationItem
import com.laarasoft.moepay.core.designsystem.native.MoeTopBar
import com.laarasoft.moepay.core.designsystem.theme.MoeTheme
import com.laarasoft.moepay.home.presentation.resources.Res
import com.laarasoft.moepay.home.presentation.resources.activity_empty_message
import com.laarasoft.moepay.home.presentation.resources.activity_empty_title
import com.laarasoft.moepay.home.presentation.resources.cancel
import com.laarasoft.moepay.home.presentation.resources.home_empty_message
import com.laarasoft.moepay.home.presentation.resources.home_empty_title
import com.laarasoft.moepay.home.presentation.resources.logout
import com.laarasoft.moepay.home.presentation.resources.logout_dialog_message
import com.laarasoft.moepay.home.presentation.resources.logout_dialog_title
import com.laarasoft.moepay.home.presentation.resources.profile_empty_message
import com.laarasoft.moepay.home.presentation.resources.profile_empty_title
import com.laarasoft.moepay.home.presentation.resources.tab_activity
import com.laarasoft.moepay.home.presentation.resources.tab_home
import com.laarasoft.moepay.home.presentation.resources.tab_profile
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeRoot(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(state = state, onAction = viewModel::onAction)
}

private data class TabSpec(
    val tab: HomeTab,
    val label: StringResource,
    val icon: MoeIcon,
    val selectedIcon: MoeIcon,
    val emptyTitle: StringResource,
    val emptyMessage: StringResource,
)

private val tabs = listOf(
    TabSpec(HomeTab.HOME, Res.string.tab_home, MoeIcon.Home, MoeIcon.HomeFilled, Res.string.home_empty_title, Res.string.home_empty_message),
    TabSpec(HomeTab.ACTIVITY, Res.string.tab_activity, MoeIcon.Activity, MoeIcon.ActivityFilled, Res.string.activity_empty_title, Res.string.activity_empty_message),
    TabSpec(HomeTab.PROFILE, Res.string.tab_profile, MoeIcon.Profile, MoeIcon.ProfileFilled, Res.string.profile_empty_title, Res.string.profile_empty_message),
)

@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
) {
    val selected = tabs.first { it.tab == state.selectedTab }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        MoeTopBar(
            title = stringResource(selected.label),
            actions = listOf(
                MoeBarAction(
                    icon = MoeIcon.Logout,
                    contentDescription = stringResource(Res.string.logout),
                    onClick = { onAction(HomeAction.OnLogoutClick) },
                )
            ),
        )

        AnimatedContent(
            targetState = selected,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "homeTab",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { tab ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                MoeEmptyState(
                    icon = tab.icon,
                    title = stringResource(tab.emptyTitle),
                    message = stringResource(tab.emptyMessage),
                )
            }
        }

        MoeNavigationBar(
            items = tabs.map {
                MoeNavigationItem(
                    label = stringResource(it.label),
                    icon = it.icon,
                    selectedIcon = it.selectedIcon,
                )
            },
            selectedIndex = tabs.indexOf(selected),
            onItemSelected = { index -> onAction(HomeAction.OnTabSelected(tabs[index].tab)) },
        )
    }

    if (state.showLogoutDialog) {
        MoeAlertDialog(
            title = stringResource(Res.string.logout_dialog_title),
            message = stringResource(Res.string.logout_dialog_message),
            confirmButton = MoeDialogButton(
                text = stringResource(Res.string.logout),
                onClick = { onAction(HomeAction.OnConfirmLogout) },
                style = MoeDialogButtonStyle.DESTRUCTIVE,
            ),
            dismissButton = MoeDialogButton(
                text = stringResource(Res.string.cancel),
                onClick = { onAction(HomeAction.OnDismissLogoutDialog) },
                style = MoeDialogButtonStyle.CANCEL,
            ),
            onDismissRequest = { onAction(HomeAction.OnDismissLogoutDialog) },
        )
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    MoeTheme { HomeScreen(state = HomeState(), onAction = {}) }
}
