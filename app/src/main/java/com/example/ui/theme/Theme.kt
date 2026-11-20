package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = StudioBlue,
    onPrimary = Color.White,
    primaryContainer = ObsidianElevated,
    onPrimaryContainer = TextPrimaryDark,
    secondary = VipViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF281E47),
    onSecondaryContainer = Color(0xFFE7DDFF),
    tertiary = VipGold,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = TextPrimaryDark,
    surface = ObsidianSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLow = ObsidianSurface,
    surfaceContainer = ObsidianCard,
    surfaceContainerHigh = ObsidianElevated,
    outline = ObsidianBorder,
    error = CoralAlert,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = StudioBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEBFF),
    onPrimaryContainer = Color(0xFF003266),
    secondary = VipVioletDeep,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE4FF),
    onSecondaryContainer = Color(0xFF2B1065),
    tertiary = VipGoldDeep,
    onTertiary = Color.White,
    background = PorcelainWhite,
    onBackground = TextPrimaryLight,
    surface = PorcelainSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = PorcelainCard,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainerLow = PorcelainSurface,
    surfaceContainer = PorcelainCard,
    surfaceContainerHigh = PorcelainElevated,
    outline = PorcelainBorder,
    error = CoralAlert,
    onError = Color.White
)

val LuminaShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

data class LuminaExtraColors(
    val glassFill: Color,
    val glassBorder: Color,
    val isDark: Boolean
)

val LocalLuminaColors = staticCompositionLocalOf {
    LuminaExtraColors(
        glassFill = Color(0xB3161622),
        glassBorder = ObsidianBorder,
        isDark = true
    )
}

@Composable
fun LuminaTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extras = if (darkTheme) {
        LuminaExtraColors(
            glassFill = Color(0xBD151520),
            glassBorder = ObsidianBorder,
            isDark = true
        )
    } else {
        LuminaExtraColors(
            glassFill = Color(0xCCFFFFFF),
            glassBorder = PorcelainBorder,
            isDark = false
        )
    }
    CompositionLocalProvider(LocalLuminaColors provides extras) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LuminaTypography,
            shapes = LuminaShapes,
            content = content
        )
    }
}
