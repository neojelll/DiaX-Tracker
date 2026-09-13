package com.neojelll.diaxtracker.ui.theme

import androidx.compose.ui.graphics.Color

// Used by glucoseColor() below and by GlucoseColorTest; names/values must stay stable even
// though the redesigned UI no longer tints record text with them directly.
val SproutGreen = Color(0xFF15803D)
val DangerRed = Color(0xFFDC2626)
val CriticalRed = Color(0xFF991B1B)
val WarningOrange = Color(0xFFD97706)
val WarningYellow = Color(0xFFEAB308)

// Fixed clinical hypoglycemia thresholds (AGP report low/very-low bands) — these never
// move with the user's chosen target range, unlike the high side below.
private const val VERY_LOW_MMOL = 3.0f
private const val LOW_MMOL = 3.9f

// How far above the user's high bound is shown as a warning before escalating further,
// matching the AGP "high" zone width (180-250 mg/dL).
private const val HIGH_ALERT_MARGIN_MMOL = 3.9f

// Absorbs float rounding noise in highBound + HIGH_ALERT_MARGIN_MMOL near the boundary.
private const val BOUNDARY_EPSILON_MMOL = 0.001f

// Clinical severity of a reading. The redesigned UI is monochrome, so this now only gates
// whether a neutral out-of-range badge is shown — never used to tint text or values.
fun glucoseColor(value: Float, lowBound: Float, highBound: Float): Color = when {
    value < VERY_LOW_MMOL -> CriticalRed
    value < maxOf(lowBound, LOW_MMOL) -> DangerRed
    value <= highBound -> SproutGreen
    value <= highBound + HIGH_ALERT_MARGIN_MMOL + BOUNDARY_EPSILON_MMOL -> WarningYellow
    else -> WarningOrange
}

/** True when a reading falls outside the user's target range (gates the neutral history badge). */
fun isOutOfRange(value: Float, lowBound: Float, highBound: Float): Boolean =
    value < lowBound || value > highBound
