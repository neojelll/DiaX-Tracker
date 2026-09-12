package com.neojelll.diaxtracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DiaXTrackerColorScheme = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    primaryContainer = FieldTile,
    onPrimaryContainer = Ink,
    secondary = Ink,
    onSecondary = Color.White,
    secondaryContainer = FieldTile,
    onSecondaryContainer = Ink,
    tertiary = WarningOrange,
    onTertiary = Color.White,
    tertiaryContainer = FieldTile,
    onTertiaryContainer = Ink,
    error = DangerRed,
    onError = Color.White,
    background = PageBackground,
    onBackground = Ink,
    surface = CardSurface,
    onSurface = Ink,
    surfaceVariant = FieldTile,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight,
)

@Composable
fun DiaXTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DiaXTrackerColorScheme,
        typography = DiaXTrackerTypography,
        content = content
    )
}
