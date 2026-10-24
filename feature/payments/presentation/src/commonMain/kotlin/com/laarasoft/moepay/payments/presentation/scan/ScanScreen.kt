package com.laarasoft.moepay.payments.presentation.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.laarasoft.moepay.core.designsystem.components.MoeButton
import com.laarasoft.moepay.core.designsystem.components.MoeButtonStyle
import com.laarasoft.moepay.core.designsystem.components.MoeIconTile
import com.laarasoft.moepay.core.designsystem.components.MoeTextField
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.designsystem.native.MoeBarAction
import com.laarasoft.moepay.core.designsystem.native.MoeTopBar
import com.laarasoft.moepay.core.designsystem.native.rememberQrScanner
import com.laarasoft.moepay.core.designsystem.theme.extended
import com.laarasoft.moepay.payments.presentation.resources.Res
import com.laarasoft.moepay.payments.presentation.resources.back
import com.laarasoft.moepay.payments.presentation.resources.scan_continue
import com.laarasoft.moepay.payments.presentation.resources.scan_message
import com.laarasoft.moepay.payments.presentation.resources.scan_open_camera
import com.laarasoft.moepay.payments.presentation.resources.scan_paste_placeholder
import com.laarasoft.moepay.payments.presentation.resources.scan_title
import com.laarasoft.moepay.payments.presentation.resources.scan_unavailable
import org.jetbrains.compose.resources.stringResource

/**
 * Opens the system QR scanner straight away; what it reads goes to [onPayload]. If there's no
 * camera scanner (simulators) or the user cancels, they can paste a payment link instead.
 */
@Composable
fun ScanRoot(
    onBack: () -> Unit,
    onPayload: (String) -> Unit,
) {
    val pasteState = remember { TextFieldState() }
    var hasAutoLaunched by remember { mutableStateOf(false) }
    val scanner = rememberQrScanner { payload ->
        if (!payload.isNullOrBlank()) onPayload(payload)
    }
    LaunchedEffect(Unit) {
        if (!hasAutoLaunched && scanner.isAvailable) {
            hasAutoLaunched = true
            scanner.scan()
        }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        MoeTopBar(title = "", navigationAction = MoeBarAction(MoeIcon.Back, stringResource(Res.string.back), onBack))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(40.dp))
            MoeIconTile(MoeIcon.Scan, size = 80.dp, inverted = true)
            Spacer(Modifier.height(20.dp))
            Text(stringResource(Res.string.scan_title), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(if (scanner.isAvailable) Res.string.scan_message else Res.string.scan_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            if (scanner.isAvailable) {
                MoeButton(stringResource(Res.string.scan_open_camera), { scanner.scan() }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(20.dp))
            }
            MoeTextField(
                state = pasteState,
                placeholder = stringResource(Res.string.scan_paste_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false, imeAction = ImeAction.Go),
                onKeyboardAction = { pasteState.text.toString().trim().takeIf { it.isNotEmpty() }?.let(onPayload) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            MoeButton(
                text = stringResource(Res.string.scan_continue),
                onClick = { onPayload(pasteState.text.toString().trim()) },
                enabled = pasteState.text.isNotBlank(),
                style = MoeButtonStyle.SECONDARY,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
