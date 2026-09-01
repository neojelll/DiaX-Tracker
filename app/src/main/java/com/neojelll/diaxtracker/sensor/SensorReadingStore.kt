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

    companion object {
        private const val PREFS_NAME = "sensor_readings"
        private const val KEY_VALUE = "latest_mmol"
        private const val KEY_TIMESTAMP = "latest_timestamp"
        const val DEFAULT_MAX_AGE_MILLIS = 15 * 60 * 1000L
    }
}
