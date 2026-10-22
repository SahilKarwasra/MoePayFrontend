package com.laarasoft.moepay.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Tokens Material's ColorScheme has no slot for. Read with `MaterialTheme.colorScheme.extended`. */
@Immutable
data class ExtendedColors(
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,
    val fieldBackground: Color,
    val surfaceRaised: Color,
    val border: Color,
    val borderStrong: Color,
    val disabledFill: Color,
)

private val LightExtendedColors = ExtendedColors(
    textPrimary = Black,
    textSecondary = Gray600,
    textTertiary = Gray500,
    textDisabled = Gray400,
    fieldBackground = Gray100,
    surfaceRaised = Gray50,
    border = Gray200,
    borderStrong = Black,
    disabledFill = Gray200,
)

private val DarkExtendedColors = ExtendedColors(
    textPrimary = White,
    textSecondary = Gray400,
    textTertiary = Gray500,
    textDisabled = Gray600,
    fieldBackground = Gray900,
    surfaceRaised = Gray950,
    border = Gray800,
    borderStrong = White,
    disabledFill = Gray800,
)

private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = Gray100,
    onPrimaryContainer = Black,
    secondary = Gray600,
    onSecondary = White,
    secondaryContainer = Gray100,
    onSecondaryContainer = Black,
    tertiary = Gray700,
    onTertiary = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    surfaceContainer = Gray100,
    surfaceContainerHigh = White,
    surfaceContainerHighest = Gray100,
    outline = Gray200,
    outlineVariant = Gray100,
    error = ErrorLight,
    onError = White,
    inverseSurface = Black,
    inverseOnSurface = White,
    inversePrimary = White,
    scrim = Black,
)

private val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    primaryContainer = Gray900,
    onPrimaryContainer = White,
    secondary = Gray400,
    onSecondary = Black,
    secondaryContainer = Gray900,
    onSecondaryContainer = White,
    tertiary = Gray300,
    onTertiary = Black,
    background = Black,
    onBackground = White,
    surface = Black,
    onSurface = White,
    surfaceVariant = Gray900,
    onSurfaceVariant = Gray400,
    surfaceContainer = Gray950,
    surfaceContainerHigh = Gray900,
    surfaceContainerHighest = Gray900,
    outline = Gray800,
    outlineVariant = Gray900,
    error = ErrorDark,
    onError = Black,
    inverseSurface = White,
    inverseOnSurface = Black,
    inversePrimary = Black,
    scrim = Black,
)

private val MoeShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

private val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val ColorScheme.extended: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current

@Composable
fun MoeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalExtendedColors provides if (darkTheme) DarkExtendedColors else LightExtendedColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = MoeTypography,
            shapes = MoeShapes,
            content = content,
        )
    }
}
