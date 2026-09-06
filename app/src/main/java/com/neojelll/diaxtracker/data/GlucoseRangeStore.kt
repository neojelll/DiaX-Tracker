package com.neojelll.diaxtracker.data

import android.content.Context

data class GlucoseRange(val low: Float, val high: Float)

class GlucoseRangeStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getRange(): GlucoseRange = GlucoseRange(
        low = prefs.getFloat(KEY_LOW, DEFAULT_LOW),
        high = prefs.getFloat(KEY_HIGH, DEFAULT_HIGH)
    )

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
    }
}
