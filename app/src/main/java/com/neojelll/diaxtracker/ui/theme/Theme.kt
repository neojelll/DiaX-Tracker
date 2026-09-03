package com.neojelll.diaxtracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DiaXTrackerColorScheme = lightColorScheme(
    primary = MossGreen,
    onPrimary = Color.White,
    primaryContainer = PaleLime,
    onPrimaryContainer = DeepForest,
    inversePrimary = SproutGreen,
    secondary = SproutGreen,
    onSecondary = DeepForest,
    secondaryContainer = PaleLime,
    onSecondaryContainer = DeepForest,
    tertiary = SproutGreen,
    onTertiary = DeepForest,
    tertiaryContainer = PaleLime,
    onTertiaryContainer = DeepForest,
    background = Color.White,
    onBackground = DeepForest,
    surface = Color.White,
    onSurface = DeepForest,
    surfaceVariant = PaleLime,
    onSurfaceVariant = DeepForest,
    outline = MossGreen,
)

@Composable
fun DiaXTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DiaXTrackerColorScheme,
        typography = DiaXTrackerTypography,
        content = content
    )
}
