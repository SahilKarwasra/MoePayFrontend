package com.laarasoft.moepay.core.designsystem.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.laarasoft.moepay.core.designsystem.resources.Res
import com.laarasoft.moepay.core.designsystem.resources.ic_activity
import com.laarasoft.moepay.core.designsystem.resources.ic_add
import com.laarasoft.moepay.core.designsystem.resources.ic_arrow_up
import com.laarasoft.moepay.core.designsystem.resources.ic_back
import com.laarasoft.moepay.core.designsystem.resources.ic_backspace
import com.laarasoft.moepay.core.designsystem.resources.ic_bank
import com.laarasoft.moepay.core.designsystem.resources.ic_chat
import com.laarasoft.moepay.core.designsystem.resources.ic_check
import com.laarasoft.moepay.core.designsystem.resources.ic_check_circle
import com.laarasoft.moepay.core.designsystem.resources.ic_chevron_right
import com.laarasoft.moepay.core.designsystem.resources.ic_close
import com.laarasoft.moepay.core.designsystem.resources.ic_clock
import com.laarasoft.moepay.core.designsystem.resources.ic_close_circle
import com.laarasoft.moepay.core.designsystem.resources.ic_copy
import com.laarasoft.moepay.core.designsystem.resources.ic_delete
import com.laarasoft.moepay.core.designsystem.resources.ic_edit
import com.laarasoft.moepay.core.designsystem.resources.ic_error
import com.laarasoft.moepay.core.designsystem.resources.ic_eye
import com.laarasoft.moepay.core.designsystem.resources.ic_eye_off
import com.laarasoft.moepay.core.designsystem.resources.ic_home
import com.laarasoft.moepay.core.designsystem.resources.ic_home_filled
import com.laarasoft.moepay.core.designsystem.resources.ic_info
import com.laarasoft.moepay.core.designsystem.resources.ic_lock
import com.laarasoft.moepay.core.designsystem.resources.ic_logout
import com.laarasoft.moepay.core.designsystem.resources.ic_pending
import com.laarasoft.moepay.core.designsystem.resources.ic_profile
import com.laarasoft.moepay.core.designsystem.resources.ic_qr
import com.laarasoft.moepay.core.designsystem.resources.ic_reply
import com.laarasoft.moepay.core.designsystem.resources.ic_request
import com.laarasoft.moepay.core.designsystem.resources.ic_scan
import com.laarasoft.moepay.core.designsystem.resources.ic_search
import com.laarasoft.moepay.core.designsystem.resources.ic_send
import com.laarasoft.moepay.core.designsystem.resources.ic_share
import com.laarasoft.moepay.core.designsystem.resources.ic_star
import com.laarasoft.moepay.core.designsystem.resources.ic_wallet
import com.laarasoft.moepay.core.designsystem.resources.ic_withdraw
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * One icon, two renderings: a vector drawable for Compose / Material (Android) and
 * an SF Symbol for the native UIKit bars on iOS.
 */
enum class MoeIcon(
    val drawable: DrawableResource,
    val sfSymbol: String,
) {
    Back(Res.drawable.ic_back, "chevron.backward"),
    Close(Res.drawable.ic_close, "xmark"),
    Home(Res.drawable.ic_home, "house"),
    HomeFilled(Res.drawable.ic_home_filled, "house.fill"),
    Wallet(Res.drawable.ic_wallet, "wallet.bifold"),
    WalletFilled(Res.drawable.ic_wallet, "wallet.bifold.fill"),
    Chat(Res.drawable.ic_chat, "bubble.left.and.bubble.right"),
    ChatFilled(Res.drawable.ic_chat, "bubble.left.and.bubble.right.fill"),
    Activity(Res.drawable.ic_activity, "clock.arrow.circlepath"),
    Profile(Res.drawable.ic_profile, "person.crop.circle"),
    Logout(Res.drawable.ic_logout, "rectangle.portrait.and.arrow.right"),
    Send(Res.drawable.ic_send, "arrow.up.right"),
    Request(Res.drawable.ic_request, "arrow.down.left"),
    Scan(Res.drawable.ic_scan, "qrcode.viewfinder"),
    Qr(Res.drawable.ic_qr, "qrcode"),
    Add(Res.drawable.ic_add, "plus"),
    Withdraw(Res.drawable.ic_withdraw, "arrow.down.to.line"),
    Bank(Res.drawable.ic_bank, "building.columns"),
    Copy(Res.drawable.ic_copy, "doc.on.doc"),
    Share(Res.drawable.ic_share, "square.and.arrow.up"),
    Eye(Res.drawable.ic_eye, "eye"),
    EyeOff(Res.drawable.ic_eye_off, "eye.slash"),
    Lock(Res.drawable.ic_lock, "lock"),
    ChevronRight(Res.drawable.ic_chevron_right, "chevron.right"),
    Search(Res.drawable.ic_search, "magnifyingglass"),
    Check(Res.drawable.ic_check, "checkmark"),
    CheckCircle(Res.drawable.ic_check_circle, "checkmark.circle.fill"),
    CloseCircle(Res.drawable.ic_close_circle, "xmark.circle.fill"),
    Star(Res.drawable.ic_star, "star.fill"),
    Backspace(Res.drawable.ic_backspace, "delete.left"),
    Error(Res.drawable.ic_error, "exclamationmark.circle"),
    Info(Res.drawable.ic_info, "info.circle"),
    Compose(Res.drawable.ic_edit, "square.and.pencil"),
    Edit(Res.drawable.ic_edit, "pencil"),
    Reply(Res.drawable.ic_reply, "arrowshape.turn.up.left"),
    ArrowUp(Res.drawable.ic_arrow_up, "arrow.up"),
    Clock(Res.drawable.ic_clock, "clock"),
    Delete(Res.drawable.ic_delete, "trash"),
    Pending(Res.drawable.ic_pending, "hourglass"),
}

@Composable
fun MoeIcon.painter(): Painter = painterResource(drawable)
