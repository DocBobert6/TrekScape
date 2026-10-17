package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.preferences.AppThemeMode

@Immutable
data class TrekScapeColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val barBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val separator: Color,
    val capsuleBg: Color,
    val cardBg: Color,
    val accent: Color,
    val isDark: Boolean
)

val LocalTrekScapeColors = staticCompositionLocalOf {
    TrekScapeColors(
        background = LightBackground,
        surface = LightSurface,
        surfaceVariant = LightSurfaceVariant,
        barBackground = LightBarBackground,
        textPrimary = LightTextPrimary,
        textSecondary = LightTextSecondary,
        separator = LightSeparator,
        capsuleBg = LightCapsuleBg,
        cardBg = LightCardBg,
        accent = TrekBluePrimary,
        isDark = false
    )
}

private val LightTrekColors = TrekScapeColors(
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    barBackground = LightBarBackground,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    separator = LightSeparator,
    capsuleBg = LightCapsuleBg,
    cardBg = LightCardBg,
    accent = TrekBluePrimary,
    isDark = false
)

private val DarkTrekColors = TrekScapeColors(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    barBackground = DarkBarBackground,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    separator = DarkSeparator,
    capsuleBg = DarkCapsuleBg,
    cardBg = DarkCardBg,
    accent = TrekBlueDark,
    isDark = true
)

private val PrivateTrekColors = TrekScapeColors(
    background = PrivateBackground,
    surface = PrivateSurface,
    surfaceVariant = Color(0xFF161F33),
    barBackground = Color(0xF2070A12),
    textPrimary = Color(0xFFF1F5F9),
    textSecondary = Color(0xFF94A3B8),
    separator = Color(0xFF232D42),
    capsuleBg = PrivateCapsuleBg,
    cardBg = PrivateSurface,
    accent = PrivateAccent,
    isDark = true
)

private val M3DarkScheme = darkColorScheme(
    primary = TrekBlueDark,
    onPrimary = Color(0xFF003258),
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkSeparator
)

private val M3LightScheme = lightColorScheme(
    primary = TrekBluePrimary,
    onPrimary = Color.White,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightSeparator
)

@Composable
fun TrekScapeTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    isPrivate: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when {
        isPrivate -> true
        themeMode == AppThemeMode.DARK -> true
        themeMode == AppThemeMode.LIGHT -> false
        else -> systemInDark
    }

    val customColors = when {
        isPrivate -> PrivateTrekColors
        isDark -> DarkTrekColors
        else -> LightTrekColors
    }

    val m3Scheme = if (isDark) M3DarkScheme else M3LightScheme

    CompositionLocalProvider(LocalTrekScapeColors provides customColors) {
        MaterialTheme(
            colorScheme = m3Scheme,
            typography = Typography,
            content = content
        )
    }
}

object TrekTheme {
    val colors: TrekScapeColors
        @Composable
        get() = LocalTrekScapeColors.current
}
