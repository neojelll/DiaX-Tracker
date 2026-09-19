package com.neojelll.diaxtracker.data

import android.content.Context

data class GlucoseRange(val low: Float, val high: Float)

class GlucoseRangeStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getRange(): GlucoseRange {
        val stored = GlucoseRange(
            low = prefs.getFloat(KEY_LOW, DEFAULT_LOW),
            high = prefs.getFloat(KEY_HIGH, DEFAULT_HIGH)
        )
        val clamped = GlucoseRange(
            low = stored.low.coerceIn(MIN_BOUND_MMOL, MAX_BOUND_MMOL),
            high = stored.high.coerceIn(MIN_BOUND_MMOL, MAX_BOUND_MMOL)
        )
        // Self-heal a value saved under a since-tightened bound (e.g. the old 20.0 mmol/L max)
        // so it doesn't linger above the current cap until the user happens to edit it.
        if (clamped != stored) saveRange(clamped)
        return clamped
    }

    fun saveRange(range: GlucoseRange) {
        prefs.edit()
            .putFloat(KEY_LOW, range.low)
            .putFloat(KEY_HIGH, range.high)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "glucose_range"
        private const val KEY_LOW = "low_mmol"
        private const val KEY_HIGH = "high_mmol"

        // International consensus target range for CGM data (Battelino et al., Diabetes Care 2019).
        const val DEFAULT_LOW = 3.9f
        const val DEFAULT_HIGH = 10.0f

        // A personal target can't cross these fixed clinical thresholds.
        const val MIN_BOUND_MMOL = 3.9f
        const val MAX_BOUND_MMOL = 15f
    }
}
