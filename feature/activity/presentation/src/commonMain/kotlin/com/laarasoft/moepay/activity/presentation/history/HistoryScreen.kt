package com.laarasoft.moepay.activity.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.activity.domain.HistoryFilter
import com.laarasoft.moepay.activity.presentation.resources.Res
import com.laarasoft.moepay.activity.presentation.resources.back
import com.laarasoft.moepay.activity.presentation.resources.empty_filtered
import com.laarasoft.moepay.activity.presentation.resources.empty_message
import com.laarasoft.moepay.activity.presentation.resources.empty_title
import com.laarasoft.moepay.activity.presentation.resources.filter_all
import com.laarasoft.moepay.activity.presentation.resources.filter_failed
import com.laarasoft.moepay.activity.presentation.resources.filter_in
import com.laarasoft.moepay.activity.presentation.resources.filter_out
import com.laarasoft.moepay.activity.presentation.resources.history
import com.laarasoft.moepay.activity.presentation.resources.load_error
import com.laarasoft.moepay.activity.presentation.resources.retry
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeButtonStyle
import com.laarasoft.moepay.core.designsystem.components.MoeCard
import com.laarasoft.moepay.core.designsystem.components.MoeEmptyState
import com.laarasoft.moepay.core.designsystem.components.MoeFilterChip
import com.laarasoft.moepay.core.designsystem.components.MoeRowDivider
import com.laarasoft.moepay.core.designsystem.components.MoeSkeletonRows
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.native.MoeBarAction
import com.laarasoft.moepay.core.designsystem.native.MoeTopBar
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.presentation.util.groupByDay
import com.laarasoft.moepay.core.presentation.util.toDayLabel
import com.laarasoft.moepay.core.ui.TransactionItem
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryRoot(
    onBack: () -> Unit,
    onTransactionClick: (String) -> Unit,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HistoryScreen(state = state, onAction = viewModel::onAction, onBack = onBack, onTransactionClick = onTransactionClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    state: HistoryState,
    onAction: (HistoryAction) -> Unit,
    onBack: () -> Unit,
    onTransactionClick: (String) -> Unit,
) {
    val sections = remember(state.items) { state.items.groupByDay { it.createdAt } }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        MoeTopBar(title = stringResource(Res.string.history), navigationAction = MoeBarAction(MoeIcon.Back, stringResource(Res.string.back), onBack))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            listOf(
                HistoryFilter.ALL to Res.string.filter_all,
                HistoryFilter.MONEY_IN to Res.string.filter_in,
                HistoryFilter.MONEY_OUT to Res.string.filter_out,
                HistoryFilter.FAILED to Res.string.filter_failed,
            ).forEach { (filter, label) ->
                MoeFilterChip(stringResource(label), state.filter == filter, { onAction(HistoryAction.OnFilterChange(filter)) })
            }
        }
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(HistoryAction.OnRefresh) },
            modifier = Modifier.weight(1f),
        ) {
            when {
                state.isLoading && state.items.isEmpty() -> MoeCard(Modifier.padding(20.dp)) { MoeSkeletonRows(count = 6) }
                state.error != null && state.items.isEmpty() -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().padding(top = 60.dp)) {
                    MoeEmptyState(MoeIcon.Error, stringResource(Res.string.load_error), state.error.asString())
                    MoeButton(stringResource(Res.string.retry), { onAction(HistoryAction.OnRefresh) }, style = MoeButtonStyle.TEXT)
                }
                state.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    MoeEmptyState(
                        icon = MoeIcon.Activity,
                        title = stringResource(Res.string.empty_title),
                        message = stringResource(if (state.filter == HistoryFilter.ALL) Res.string.empty_message else Res.string.empty_filtered),
                    )
                }
                else -> LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp), modifier = Modifier.fillMaxSize()) {
                    sections.forEachIndexed { sectionIndex, (day, transactions) ->
                        item(key = "header-$day") {
                            Text(
                                day.toDayLabel().asString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.extended.textSecondary,
                                modifier = Modifier.padding(top = if (sectionIndex == 0) 8.dp else 20.dp, bottom = 8.dp).animateItem(),
                            )
                        }
                        item(key = "section-$day") {
                            MoeCard(Modifier.animateItem()) {
                                transactions.forEachIndexed { index, transaction ->
                                    if (index > 0) MoeRowDivider()
                                    TransactionItem(transaction = transaction, showDay = false, onClick = { onTransactionClick(transaction.id) })
                                }
                            }
                        }
                    }
                    item(key = "footer") {
                        LaunchedEffect(state.items.size, state.hasMore) { if (state.hasMore) onAction(HistoryAction.OnLoadMore) }
                        if (state.isLoadingMore) {
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
