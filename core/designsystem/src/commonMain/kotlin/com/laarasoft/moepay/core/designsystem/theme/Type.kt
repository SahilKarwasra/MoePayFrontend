package com.laarasoft.moepay.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// FontFamily.Default resolves to SF Pro on iOS and Roboto on Android, so each platform reads natively.
private fun style(size: Int, lineHeight: Int, weight: FontWeight, tracking: Double = 0.0) = TextStyle(
    fontFamily = FontFamily.Default,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
    letterSpacing = tracking.em,
)

internal val MoeTypography = Typography(
    displayLarge = style(44, 50, FontWeight.Bold, -0.03),
    displayMedium = style(36, 42, FontWeight.Bold, -0.03),
    displaySmall = style(32, 38, FontWeight.Bold, -0.02),
    headlineLarge = style(30, 36, FontWeight.Bold, -0.02),
    headlineMedium = style(26, 32, FontWeight.Bold, -0.02),
    headlineSmall = style(22, 28, FontWeight.SemiBold, -0.01),
    titleLarge = style(20, 26, FontWeight.SemiBold, -0.01),
    titleMedium = style(17, 22, FontWeight.SemiBold),
    titleSmall = style(15, 20, FontWeight.SemiBold),
    bodyLarge = style(17, 24, FontWeight.Normal),
    bodyMedium = style(15, 21, FontWeight.Normal),
    bodySmall = style(13, 18, FontWeight.Normal),
    labelLarge = style(16, 20, FontWeight.SemiBold),
    labelMedium = style(13, 16, FontWeight.Medium),
    labelSmall = style(11, 14, FontWeight.Medium, 0.02),
)
