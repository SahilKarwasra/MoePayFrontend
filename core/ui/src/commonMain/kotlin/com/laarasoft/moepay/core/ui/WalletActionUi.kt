package com.laarasoft.moepay.core.ui

import androidx.compose.runtime.Composable
import com.laarasoft.moepay.core.designsystem.icon.MoeIcon
import com.laarasoft.moepay.core.domain.wallet.WalletActionId
import com.laarasoft.moepay.core.ui.resources.Res
import com.laarasoft.moepay.core.ui.resources.action_add_money
import com.laarasoft.moepay.core.ui.resources.action_history
import com.laarasoft.moepay.core.ui.resources.action_request
import com.laarasoft.moepay.core.ui.resources.action_scan
import com.laarasoft.moepay.core.ui.resources.action_send
import com.laarasoft.moepay.core.ui.resources.action_withdraw
import org.jetbrains.compose.resources.stringResource

val WalletActionId.icon: MoeIcon
    get() = when (this) {
        WalletActionId.SEND -> MoeIcon.Send
        WalletActionId.REQUEST -> MoeIcon.Request
        WalletActionId.SCAN_QR -> MoeIcon.Scan
        WalletActionId.ADD_MONEY -> MoeIcon.Add
        WalletActionId.WITHDRAW -> MoeIcon.Withdraw
        WalletActionId.HISTORY -> MoeIcon.Activity
        WalletActionId.UNKNOWN -> MoeIcon.Info
    }

@Composable
fun WalletActionId.label(): String = when (this) {
    WalletActionId.SEND -> stringResource(Res.string.action_send)
    WalletActionId.REQUEST -> stringResource(Res.string.action_request)
    WalletActionId.SCAN_QR -> stringResource(Res.string.action_scan)
    WalletActionId.ADD_MONEY -> stringResource(Res.string.action_add_money)
    WalletActionId.WITHDRAW -> stringResource(Res.string.action_withdraw)
    WalletActionId.HISTORY -> stringResource(Res.string.action_history)
    WalletActionId.UNKNOWN -> ""
}
