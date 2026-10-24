package com.laarasoft.moepay.payments.presentation.requests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeButtonStyle
import com.laarasoft.moepay.core.designsystem.components.MoeEmptyState
import com.laarasoft.moepay.core.designsystem.components.MoeFilterChip
import com.laarasoft.moepay.core.designsystem.components.MoeRowDivider
import com.laarasoft.moepay.core.designsystem.components.MoeSkeletonRows
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.native.MoeBarAction
import com.laarasoft.moepay.core.designsystem.native.MoeSegmentedControl
import com.laarasoft.moepay.core.designsystem.native.MoeTopBar
import com.laarasoft.moepay.core.domain.money.MoneyRequestDirection
import com.laarasoft.moepay.core.ui.MoneyRequestItem
import com.laarasoft.moepay.payments.presentation.resources.Res
import com.laarasoft.moepay.payments.presentation.resources.back
import com.laarasoft.moepay.payments.presentation.resources.filter_all
import com.laarasoft.moepay.payments.presentation.resources.filter_pending
import com.laarasoft.moepay.payments.presentation.resources.incoming
import com.laarasoft.moepay.payments.presentation.resources.outgoing
import com.laarasoft.moepay.payments.presentation.resources.requests
import com.laarasoft.moepay.payments.presentation.resources.requests_empty_incoming
import com.laarasoft.moepay.payments.presentation.resources.requests_empty_outgoing
import com.laarasoft.moepay.payments.presentation.resources.requests_empty_title
import com.laarasoft.moepay.payments.presentation.resources.retry
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MoneyRequestsRoot(
    onBack: () -> Unit,
    onRequestClick: (String) -> Unit,
    viewModel: MoneyRequestsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.onAction(MoneyRequestsAction.OnAppear) }
    MoneyRequestsScreen(state = state, onAction = viewModel::onAction, onBack = onBack, onRequestClick = onRequestClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyRequestsScreen(
    state: MoneyRequestsState,
    onAction: (MoneyRequestsAction) -> Unit,
    onBack: () -> Unit,
    onRequestClick: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        MoeTopBar(title = stringResource(Res.string.requests), navigationAction = MoeBarAction(MoeIcon.Back, stringResource(Res.string.back), onBack))
        MoeSegmentedControl(
            options = listOf(stringResource(Res.string.incoming), stringResource(Res.string.outgoing)),
            selectedIndex = if (state.direction == MoneyRequestDirection.INCOMING) 0 else 1,
            onSelect = { onAction(MoneyRequestsAction.OnDirectionChange(if (it == 0) MoneyRequestDirection.INCOMING else MoneyRequestDirection.OUTGOING)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            MoeFilterChip(stringResource(Res.string.filter_pending), state.pendingOnly, { onAction(MoneyRequestsAction.OnPendingOnlyChange(true)) })
            MoeFilterChip(stringResource(Res.string.filter_all), !state.pendingOnly, { onAction(MoneyRequestsAction.OnPendingOnlyChange(false)) })
        }
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(MoneyRequestsAction.OnRefresh) },
            modifier = Modifier.weight(1f),
        ) {
            when {
                state.isLoading && state.items.isEmpty() -> MoeSkeletonRows(count = 5, modifier = Modifier.padding(horizontal = 4.dp))
                state.error != null && state.items.isEmpty() -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().padding(top = 60.dp)) {
                    MoeEmptyState(MoeIcon.Error, stringResource(Res.string.requests_empty_title), state.error.asString())
                    MoeButton(stringResource(Res.string.retry), { onAction(MoneyRequestsAction.OnRefresh) }, style = MoeButtonStyle.TEXT)
                }
                state.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    MoeEmptyState(
                        icon = MoeIcon.Pending,
                        title = stringResource(Res.string.requests_empty_title),
                        message = stringResource(if (state.direction == MoneyRequestDirection.INCOMING) Res.string.requests_empty_incoming else Res.string.requests_empty_outgoing),
                    )
                }
                else -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(state.items, key = { _, item -> item.id }) { index, request ->
                        if (index > 0) MoeRowDivider()
                        MoneyRequestItem(request = request, onClick = { onRequestClick(request.id) }, modifier = Modifier.animateItem())
                        if (index >= state.items.size - 4) {
                            LaunchedEffect(state.items.size) { onAction(MoneyRequestsAction.OnLoadMore) }
                        }
                    }
                    if (state.isLoadingMore) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    }
                }
            }
        }
    }
}

