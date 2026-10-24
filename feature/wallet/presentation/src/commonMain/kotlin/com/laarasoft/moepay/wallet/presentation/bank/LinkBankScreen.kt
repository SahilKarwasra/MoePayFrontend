package com.laarasoft.moepay.wallet.presentation.bank

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laarasoft.moepay.core.designsystem.components.DigitsOnlyTransformation
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeFormLayout
import com.laarasoft.moepay.core.designsystem.components.MoeScreenHeader
import com.laarasoft.moepay.core.designsystem.components.MoeTextField
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.icon.painter
import com.laarasoft.moepay.core.designsystem.native.MoeBarAction
import com.laarasoft.moepay.core.designsystem.native.MoeSwitch
import com.laarasoft.moepay.core.designsystem.native.MoeTopBar
import com.laarasoft.moepay.core.designsystem.native.rememberHaptics
import com.laarasoft.moepay.core.presentation.util.ObserveAsEvents
import com.laarasoft.moepay.wallet.domain.BankDetailsValidator
import com.laarasoft.moepay.wallet.presentation.resources.Res
import com.laarasoft.moepay.wallet.presentation.resources.account_number
import com.laarasoft.moepay.wallet.presentation.resources.account_number_invalid
import com.laarasoft.moepay.wallet.presentation.resources.account_number_placeholder
import com.laarasoft.moepay.wallet.presentation.resources.back
import com.laarasoft.moepay.wallet.presentation.resources.holder
import com.laarasoft.moepay.wallet.presentation.resources.holder_invalid
import com.laarasoft.moepay.wallet.presentation.resources.holder_placeholder
import com.laarasoft.moepay.wallet.presentation.resources.link_bank_header
import com.laarasoft.moepay.wallet.presentation.resources.link_bank_header_subtitle
import com.laarasoft.moepay.wallet.presentation.resources.link_bank_title
import com.laarasoft.moepay.wallet.presentation.resources.link_button
import com.laarasoft.moepay.wallet.presentation.resources.make_primary_label
import com.laarasoft.moepay.wallet.presentation.resources.sort_code
import com.laarasoft.moepay.wallet.presentation.resources.sort_code_invalid
import com.laarasoft.moepay.wallet.presentation.resources.sort_code_placeholder
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Shows "123456" as "12-34-56" while the field keeps digits only. */
private val SortCodeOutput = OutputTransformation {
    if (length > 4) insert(4, "-")
    if (length > 2) insert(2, "-")
}

@Composable
fun LinkBankRoot(
    onBack: () -> Unit,
    onLinked: () -> Unit,
    viewModel: LinkBankViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptics = rememberHaptics()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            LinkBankEvent.Linked -> {
                haptics.success()
                onLinked()
            }
        }
    }
    LinkBankScreen(
        state = state,
        onAction = { action ->
            if (action == LinkBankAction.OnBack) onBack()
            viewModel.onAction(action)
        },
    )
}

@Composable
fun LinkBankScreen(
    state: LinkBankState,
    onAction: (LinkBankAction) -> Unit,
) {
    MoeFormLayout(
        topBar = {
            MoeTopBar(
                title = stringResource(Res.string.link_bank_title),
                navigationAction = MoeBarAction(MoeIcon.Back, stringResource(Res.string.back)) { onAction(LinkBankAction.OnBack) },
            )
        },
        bottomContent = {
            MoeButton(
                text = stringResource(Res.string.link_button),
                onClick = { onAction(LinkBankAction.OnSubmit) },
                enabled = state.canSubmit,
                isLoading = state.isLinking,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        MoeScreenHeader(
            title = stringResource(Res.string.link_bank_header),
            subtitle = stringResource(Res.string.link_bank_header_subtitle),
        )
        Spacer(Modifier.height(28.dp))
        MoeTextField(
            state = state.holderState,
            title = stringResource(Res.string.holder),
            placeholder = stringResource(Res.string.holder_placeholder),
            isError = state.isHolderInvalid,
            supportingText = if (state.isHolderInvalid) stringResource(Res.string.holder_invalid) else null,
            inputTransformation = InputTransformation.maxLength(BankDetailsValidator.HOLDER_NAME_MAX),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            enabled = !state.isLinking,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        MoeTextField(
            state = state.sortCodeState,
            title = stringResource(Res.string.sort_code),
            placeholder = stringResource(Res.string.sort_code_placeholder),
            isError = state.isSortCodeInvalid,
            supportingText = when {
                state.isSortCodeInvalid -> stringResource(Res.string.sort_code_invalid)
                else -> state.bankName
            },
            inputTransformation = DigitsOnlyTransformation.then(InputTransformation.maxLength(BankDetailsValidator.SORT_CODE_DIGITS)),
            outputTransformation = SortCodeOutput,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
            trailingContent = {
                when {
                    state.isLookingUpBank -> CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onBackground)
                    state.bankName != null -> Icon(MoeIcon.Bank.painter(), null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
                }
            },
            enabled = !state.isLinking,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        MoeTextField(
            state = state.accountNumberState,
            title = stringResource(Res.string.account_number),
            placeholder = stringResource(Res.string.account_number_placeholder),
            isError = state.isAccountNumberInvalid,
            supportingText = if (state.isAccountNumberInvalid) stringResource(Res.string.account_number_invalid) else null,
            inputTransformation = DigitsOnlyTransformation.then(InputTransformation.maxLength(BankDetailsValidator.ACCOUNT_NUMBER_DIGITS)),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            onKeyboardAction = { onAction(LinkBankAction.OnSubmit) },
            enabled = !state.isLinking,
            modifier = Modifier.fillMaxWidth(),
        )
        AnimatedVisibility(visible = state.showMakePrimary) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(64.dp)) {
                Text(
                    stringResource(Res.string.make_primary_label),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                MoeSwitch(checked = state.makePrimary, onCheckedChange = { onAction(LinkBankAction.OnMakePrimaryChange(it)) })
            }
        }
        state.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it.asString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}
