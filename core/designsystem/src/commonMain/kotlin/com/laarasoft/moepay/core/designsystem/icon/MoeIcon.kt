package com.laarasoft.moepay.core.designsystem.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.laarasoft.moepay.core.designsystem.resources.Res
import com.laarasoft.moepay.core.designsystem.resources.ic_activity
import com.laarasoft.moepay.core.designsystem.resources.ic_back
import com.laarasoft.moepay.core.designsystem.resources.ic_check_circle
import com.laarasoft.moepay.core.designsystem.resources.ic_close_circle
import com.laarasoft.moepay.core.designsystem.resources.ic_home
import com.laarasoft.moepay.core.designsystem.resources.ic_home_filled
import com.laarasoft.moepay.core.designsystem.resources.ic_logout
import com.laarasoft.moepay.core.designsystem.resources.ic_profile
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
    Home(Res.drawable.ic_home, "house"),
    HomeFilled(Res.drawable.ic_home_filled, "house.fill"),
    Activity(Res.drawable.ic_activity, "clock"),
    ActivityFilled(Res.drawable.ic_activity, "clock.fill"),
    Profile(Res.drawable.ic_profile, "person.crop.circle"),
    ProfileFilled(Res.drawable.ic_profile, "person.crop.circle.fill"),
    Logout(Res.drawable.ic_logout, "rectangle.portrait.and.arrow.right"),
    CheckCircle(Res.drawable.ic_check_circle, "checkmark.circle.fill"),
    CloseCircle(Res.drawable.ic_close_circle, "xmark.circle.fill"),
}

@Composable
fun MoeIcon.painter(): Painter = painterResource(drawable)
