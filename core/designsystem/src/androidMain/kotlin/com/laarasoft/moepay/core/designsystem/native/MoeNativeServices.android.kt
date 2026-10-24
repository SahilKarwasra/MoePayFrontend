package com.laarasoft.moepay.core.designsystem.native

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.laarasoft.moepay.core.designsystem.theme.extended

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun MoeSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    size: MoeSheetSize,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    ) {
        val sizeModifier = if (size == MoeSheetSize.LARGE) Modifier.fillMaxHeight(0.92f) else Modifier
        androidx.compose.foundation.layout.Column(sizeModifier.navigationBarsPadding()) { content() }
    }
}

@Composable
actual fun rememberHaptics(): MoeHaptics {
    val view = LocalView.current
    return remember(view) {
        object : MoeHaptics {
            override fun tap() {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }

            override fun selection() {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }

            override fun success() {
                view.performHapticFeedback(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
                    else HapticFeedbackConstants.CONTEXT_CLICK
                )
            }

            override fun error() {
                view.performHapticFeedback(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT
                    else HapticFeedbackConstants.LONG_PRESS
                )
            }
        }
    }
}

@Composable
actual fun rememberShareSheet(): (text: String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { text ->
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}

@Composable
actual fun rememberClipboard(): (text: String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { text ->
            val clipboard = context.getSystemService(ClipboardManager::class.java)
            clipboard?.setPrimaryClip(ClipData.newPlainText("MoePay", text))
        }
    }
}

/** Google Code Scanner: a system UI, no camera permission needed by the app. */
@Composable
actual fun rememberQrScanner(onResult: (payload: String?) -> Unit): MoeQrScanner {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)
    return remember(context) {
        val scanner = GmsBarcodeScanning.getClient(
            context,
            GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .enableAutoZoom()
                .build(),
        )
        object : MoeQrScanner {
            override val isAvailable: Boolean = true

            override fun scan() {
                scanner.startScan()
                    .addOnSuccessListener { currentOnResult(it.rawValue) }
                    .addOnCanceledListener { currentOnResult(null) }
                    .addOnFailureListener { currentOnResult(null) }
            }
        }
    }
}

@Composable
actual fun MoeSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = colorScheme.onBackground,
                    activeContentColor = colorScheme.background,
                    inactiveContainerColor = colorScheme.background,
                    inactiveContentColor = colorScheme.onBackground,
                    activeBorderColor = colorScheme.onBackground,
                    inactiveBorderColor = colorScheme.extended.border,
                ),
                icon = {},
                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
            )
        }
    }
}

@Composable
actual fun MoeSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
) {
    val colorScheme = MaterialTheme.colorScheme
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedTrackColor = colorScheme.onBackground,
            checkedThumbColor = colorScheme.background,
            uncheckedTrackColor = colorScheme.extended.fieldBackground,
            uncheckedThumbColor = colorScheme.extended.textTertiary,
            uncheckedBorderColor = colorScheme.extended.border,
        ),
        modifier = modifier,
    )
}
