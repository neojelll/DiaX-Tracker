package com.neojelll.diaxtracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DiaXTrackerColorScheme = lightColorScheme(
    primary = AccentDark,
    onPrimary = Color.White,
    primaryContainer = FieldBackground,
    onPrimaryContainer = TextPrimary,
    secondary = AccentDark,
    onSecondary = Color.White,
    secondaryContainer = FieldBackground,
    onSecondaryContainer = TextPrimary,
    tertiary = WarningOrange,
    onTertiary = Color.White,
    tertiaryContainer = FieldBackground,
    onTertiaryContainer = TextPrimary,
    error = DangerRed,
    onError = Color.White,
    background = PageBackground,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = FieldBackground,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
)

@Composable
fun DiaXTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DiaXTrackerColorScheme,
        typography = DiaXTrackerTypography,
        content = content
    )
}
