package com.neojelll.diaxtracker.ui.theme

import androidx.compose.ui.graphics.Color

// Below this, glucose is hypoglycemia regardless of the user's chosen target range
// (international consensus on time in range, Battelino et al., Diabetes Care 2019).
private const val HYPO_FLOOR_MMOL = 3.9f

// How far above the user's high bound is shown as a warning before escalating to red,
// matching the "high" zone width (180-250 mg/dL) from the same consensus.
private const val HIGH_ALERT_MARGIN_MMOL = 3.9f

// Absorbs float rounding noise in highBound + HIGH_ALERT_MARGIN_MMOL so a value entered
// exactly at the boundary (e.g. 10.9 against a 7.0 high bound) doesn't flip to red.
private const val BOUNDARY_EPSILON_MMOL = 0.001f

fun glucoseColor(value: Float, lowBound: Float, highBound: Float): Color = when {
    value < HYPO_FLOOR_MMOL -> DangerRed
    value < lowBound -> WarningOrange
    value <= highBound -> SproutGreen
    value <= highBound + HIGH_ALERT_MARGIN_MMOL + BOUNDARY_EPSILON_MMOL -> WarningOrange
    else -> DangerRed
}
