package com.neojelll.diaxtracker.data

import android.content.Context

class InsulinSettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getDurationHours(): Float {
        val stored = prefs.getFloat(KEY_DURATION_HOURS, DEFAULT_DURATION_HOURS)
        val clamped = stored.coerceIn(MIN_DURATION_HOURS, MAX_DURATION_HOURS)
        if (clamped != stored) saveDurationHours(clamped)
        return clamped
    }

    fun saveDurationHours(hours: Float) {
        prefs.edit().putFloat(KEY_DURATION_HOURS, hours).apply()
    }

    companion object {
        private const val PREFS_NAME = "insulin_settings"
        private const val KEY_DURATION_HOURS = "duration_hours"

        // Matches the flat 4h cutoff the active-insulin banner used before it modeled decay, so
        // nobody's readout changes shape until they actually touch this setting.
        const val DEFAULT_DURATION_HOURS = 4f
        const val MIN_DURATION_HOURS = 3f
        const val MAX_DURATION_HOURS = 8f
    }
}
