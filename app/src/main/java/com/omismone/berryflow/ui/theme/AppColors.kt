package com.omismone.berryflow.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// The app's semantic colors. Screens read these through AppTheme.colors
// instead of hardcoding colors, so light and dark stay consistent everywhere.
// Category colors are user data and are not part of the theme (they are drawn
// as translucent tints over the background).
@Immutable
data class AppColors(
    val isDark: Boolean,
    val background: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val border: Color,
    val keyBackground: Color,   // keypad keys, neutral buttons
    val okKey: Color,           // the keypad's confirm key
    val income: Color,          // income / positive amounts
    val expense: Color,         // expense / negative amounts, delete mode, errors
    val danger: Color           // background of destructive buttons (white text)
)

val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFFFFFFF),
    primaryText = Color(0xFF000000),
    secondaryText = Color(0xFF9E9E9E),
    border = Color(0xFFE0E0E0),
    keyBackground = Color(0xFFECECEC),
    okKey = Color(0xFF424242),
    income = Color(0xFF43A047),
    expense = Color(0xFFE53935),
    danger = Color(0xFFE53935)
)

val DarkAppColors = AppColors(
    isDark = true,
    background = Color(0xFF121212),
    primaryText = Color(0xFFEDEDED),
    secondaryText = Color(0xFF9E9E9E),
    border = Color(0xFF3A3A3A),
    keyBackground = Color(0xFF2C2C2C),
    okKey = Color(0xFF5C5C5C),
    income = Color(0xFF66BB6A),
    expense = Color(0xFFEF5350),
    danger = Color(0xFFE53935)
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}