package com.laarasoft.moepay.core.designsystem.native

import androidx.compose.ui.graphics.Color
import platform.UIKit.UIColor

internal fun Color.toUIColor(): UIColor = UIColor.colorWithRed(
    red = red.toDouble(),
    green = green.toDouble(),
    blue = blue.toDouble(),
    alpha = alpha.toDouble(),
)
