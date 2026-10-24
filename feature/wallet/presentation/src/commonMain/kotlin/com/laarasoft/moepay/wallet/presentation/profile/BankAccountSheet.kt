package com.laarasoft.moepay.wallet.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeButtonStyle
import com.laarasoft.moepay.core.designsystem.components.MoeIconTile
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.native.MoeSheet
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.core.domain.wallet.BankAccount
import com.laarasoft.moepay.core.presentation.util.UiText
import com.laarasoft.moepay.wallet.presentation.resources.Res
import com.laarasoft.moepay.wallet.presentation.resources.account_number
import com.laarasoft.moepay.wallet.presentation.resources.holder
import com.laarasoft.moepay.wallet.presentation.resources.make_primary
import com.laarasoft.moepay.wallet.presentation.resources.remove_account
import com.laarasoft.moepay.wallet.presentation.resources.sort_code
import org.jetbrains.compose.resources.stringResource

@Composable
fun BankAccountSheet(
    bank: BankAccount?,
    isBusy: Boolean,
    error: UiText?,
    onMakePrimary: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    MoeSheet(visible = bank != null, onDismissRequest = onDismiss) {
        if (bank == null) return@MoeSheet
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        ) {
            MoeIconTile(MoeIcon.Bank, size = 56.dp, inverted = true)
            Spacer(Modifier.height(12.dp))
            Text("${bank.bankName} ••${bank.last4}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(20.dp))
            Detail(stringResource(Res.string.holder), bank.holderName)
            Detail(stringResource(Res.string.sort_code), bank.sortCode)
            Detail(stringResource(Res.string.account_number), bank.accountNumberMasked)
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error.asString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(20.dp))
            if (!bank.isPrimary) {
                MoeButton(stringResource(Res.string.make_primary), onMakePrimary, isLoading = isBusy, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }
            MoeButton(stringResource(Res.string.remove_account), onRemove, style = MoeButtonStyle.TEXT, enabled = !isBusy, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Detail(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.extended.textSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
