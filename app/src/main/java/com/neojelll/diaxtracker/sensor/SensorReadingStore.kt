package com.neojelll.diaxtracker.sensor

import android.content.Context

class SensorReadingStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(bloodSugarMmol: Float, timestampMillis: Long) {
        prefs.edit()
            .putFloat(KEY_VALUE, bloodSugarMmol)
            .putLong(KEY_TIMESTAMP, timestampMillis)
            .apply()
    }

    fun getLatestReading(maxAgeMillis: Long = DEFAULT_MAX_AGE_MILLIS): Float? {
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)
        if (timestamp == 0L || System.currentTimeMillis() - timestamp > maxAgeMillis) return null
        val value = prefs.getFloat(KEY_VALUE, -1f)
        return value.takeIf { it > 0f }
    }

    /**
     * Whether any reading has arrived within [windowMillis], regardless of [DEFAULT_MAX_AGE_MILLIS].
     * Lets callers tell "actively paired with a sensor but it just dropped out" apart from "never
     * set one up" or "gave up on it a while ago" - neither of which should raise a fresh-data alarm.
     */
    fun hasReadingWithin(windowMillis: Long): Boolean {
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)
        return timestamp != 0L && System.currentTimeMillis() - timestamp <= windowMillis
    }

    companion object {
        private const val PREFS_NAME = "sensor_readings"
        private const val KEY_VALUE = "latest_mmol"
        private const val KEY_TIMESTAMP = "latest_timestamp"
        const val DEFAULT_MAX_AGE_MILLIS = 15 * 60 * 1000L
        const val RECENT_ACTIVITY_WINDOW_MILLIS = 3 * 24 * 60 * 60 * 1000L
    }
}
