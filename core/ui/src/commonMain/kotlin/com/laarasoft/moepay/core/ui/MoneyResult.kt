package com.laarasoft.moepay.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeButtonStyle
import com.laarasoft.moepay.core.designsystem.components.MoeCard
import com.laarasoft.moepay.core.designsystem.components.MoeRowDivider
import com.laarasoft.moepay.core.designsystem.components.MoeSuccessCheck
import com.laarasoft.moepay.core.designsystem.motion.fadeUpOnAppear
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.ui.resources.Res
import com.laarasoft.moepay.core.ui.resources.done
import com.laarasoft.moepay.core.ui.resources.try_again
import org.jetbrains.compose.resources.stringResource

data class ResultDetail(val label: String, val value: String)

/**
 * The end of a payment: an animated check (or cross), a headline like "£10.00 sent", and the
 * key details. [onTryAgain] shows a retry button for failures.
 */
@Composable
fun MoneyResultContent(
    isSuccess: Boolean,
    headline: String,
    message: String?,
    details: List<ResultDetail>,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    onTryAgain: (() -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
    ) {
        Spacer(Modifier.weight(0.6f))
        MoeSuccessCheck(isError = !isSuccess)
        Spacer(Modifier.height(28.dp))
        Text(
            text = headline,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fadeUpOnAppear(4),
        )
        if (message != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.extended.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fadeUpOnAppear(5),
            )
        }
        if (details.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            MoeCard(modifier = Modifier.fadeUpOnAppear(6)) {
                details.forEachIndexed { index, detail ->
                    if (index > 0) MoeRowDivider(startInset = 16.dp)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Text(detail.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.extended.textSecondary)
                        Text(detail.value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        if (onTryAgain != null) {
            MoeButton(text = stringResource(Res.string.try_again), onClick = onTryAgain, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
        }
        MoeButton(
            text = stringResource(Res.string.done),
            onClick = onDone,
            style = if (onTryAgain != null) MoeButtonStyle.SECONDARY else MoeButtonStyle.PRIMARY,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
