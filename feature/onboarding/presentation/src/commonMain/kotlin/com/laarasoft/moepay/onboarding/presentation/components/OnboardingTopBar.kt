package com.laarasoft.moepay.onboarding.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.components.MoeStepIndicator
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.native.MoeBarAction
import com.laarasoft.moepay.core.designsystem.native.MoeTopBar
import com.laarasoft.moepay.onboarding.presentation.navigation.ONBOARDING_STEP_COUNT
import com.laarasoft.moepay.onboarding.presentation.resources.Res
import com.laarasoft.moepay.onboarding.presentation.resources.back
import com.laarasoft.moepay.onboarding.presentation.resources.step_of
import org.jetbrains.compose.resources.stringResource

/** Native top bar titled "Step n of 3" with a segmented progress bar under it. */
@Composable
internal fun OnboardingTopBar(
    step: Int,
    canGoBack: Boolean,
    onBack: () -> Unit,
) {
    Column {
        MoeTopBar(
            title = stringResource(Res.string.step_of, step, ONBOARDING_STEP_COUNT),
            navigationAction = if (canGoBack) {
                MoeBarAction(
                    icon = MoeIcon.Back,
                    contentDescription = stringResource(Res.string.back),
                    onClick = onBack,
                )
            } else null,
        )
        MoeStepIndicator(
            current = step,
            total = ONBOARDING_STEP_COUNT,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
    }
}
