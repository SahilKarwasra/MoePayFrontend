package com.laarasoft.moepay.onboarding.presentation.review

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeFormLayout
import com.laarasoft.moepay.core.designsystem.components.MoeScreenHeader
import com.laarasoft.moepay.core.designsystem.theme.MoeTheme
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.presentation.util.ObserveAsEvents
import com.laarasoft.moepay.onboarding.presentation.components.OnboardingTopBar
import com.laarasoft.moepay.onboarding.presentation.resources.Res
import com.laarasoft.moepay.onboarding.presentation.resources.review_email
import com.laarasoft.moepay.onboarding.presentation.resources.review_finish
import com.laarasoft.moepay.onboarding.presentation.resources.review_not_added
import com.laarasoft.moepay.onboarding.presentation.resources.review_phone
import com.laarasoft.moepay.onboarding.presentation.resources.review_subtitle
import com.laarasoft.moepay.onboarding.presentation.resources.review_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ReviewRoot(
    canGoBack: Boolean,
    onBack: () -> Unit,
    onCompleted: () -> Unit,
    viewModel: ReviewViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ReviewEvent.Completed -> onCompleted()
        }
    }

    ReviewScreen(
        state = state,
        canGoBack = canGoBack,
        onAction = { action ->
            when (action) {
                ReviewAction.OnBackClick -> onBack()
                else -> Unit
            }
            viewModel.onAction(action)
        },
    )
}

@Composable
fun ReviewScreen(
    state: ReviewState,
    canGoBack: Boolean,
    onAction: (ReviewAction) -> Unit,
) {
    MoeFormLayout(
        topBar = {
            OnboardingTopBar(
                step = 3,
                canGoBack = canGoBack,
                onBack = { onAction(ReviewAction.OnBackClick) },
            )
        },
        bottomContent = {
            MoeButton(
                text = stringResource(Res.string.review_finish),
                onClick = { onAction(ReviewAction.OnFinishClick) },
                enabled = !state.isLoading,
                isLoading = state.isCompleting,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        MoeScreenHeader(
            title = stringResource(Res.string.review_title),
            subtitle = stringResource(Res.string.review_subtitle),
        )
        Spacer(Modifier.height(28.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground, strokeWidth = 2.dp)
            }
        } else {
            SummaryCard(state)
        }

        state.error?.let { error ->
            Spacer(Modifier.height(16.dp))
            Text(
                text = error.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun SummaryCard(state: ReviewState) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.extended.border, MaterialTheme.shapes.large)
            .padding(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .background(colorScheme.onBackground, CircleShape),
            ) {
                Text(
                    text = state.initials,
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.background,
                )
            }
            Column {
                Text(
                    text = state.fullName,
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onBackground,
                )
                Text(
                    text = "@${state.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.extended.textSecondary,
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = colorScheme.extended.border)
        DetailRow(label = stringResource(Res.string.review_phone), value = state.phoneNumber)
        HorizontalDivider(color = colorScheme.extended.border)
        DetailRow(
            label = stringResource(Res.string.review_email),
            value = state.email ?: stringResource(Res.string.review_not_added),
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.extended.textSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview
@Composable
private fun ReviewScreenPreview() {
    MoeTheme {
        ReviewScreen(
            state = ReviewState(
                isLoading = false,
                fullName = "Alex Taylor",
                initials = "AT",
                username = "alex",
                phoneNumber = "+447911123456",
            ),
            canGoBack = true,
            onAction = {},
        )
    }
}
