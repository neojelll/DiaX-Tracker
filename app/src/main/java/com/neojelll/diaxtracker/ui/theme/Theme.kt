package com.neojelll.diaxtracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DiaXTrackerColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = OnAccent,
    primaryContainer = FieldBackground,
    onPrimaryContainer = TextPrimary,
    secondary = AccentGreen,
    onSecondary = OnAccent,
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
