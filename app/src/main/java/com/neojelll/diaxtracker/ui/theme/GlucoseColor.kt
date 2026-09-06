package com.neojelll.diaxtracker.ui.theme

import androidx.compose.ui.graphics.Color

// Fixed clinical hypoglycemia thresholds (AGP report low/very-low bands) — these never
// move with the user's chosen target range, unlike the high side below.
private const val VERY_LOW_MMOL = 3.0f
private const val LOW_MMOL = 3.9f

// How far above the user's high bound is shown as a warning before escalating further,
// matching the AGP "high" zone width (180-250 mg/dL).
private const val HIGH_ALERT_MARGIN_MMOL = 3.9f

// Absorbs float rounding noise in highBound + HIGH_ALERT_MARGIN_MMOL near the boundary.
private const val BOUNDARY_EPSILON_MMOL = 0.001f

fun glucoseColor(value: Float, lowBound: Float, highBound: Float): Color = when {
    value < VERY_LOW_MMOL -> CriticalRed
    value < maxOf(lowBound, LOW_MMOL) -> DangerRed
    value <= highBound -> SproutGreen
    value <= highBound + HIGH_ALERT_MARGIN_MMOL + BOUNDARY_EPSILON_MMOL -> WarningYellow
    else -> WarningOrange
}
