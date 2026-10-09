package com.neojelll.diaxtracker.sensor

import android.content.Context

/** The last value that arrived from the sensor source, and when (epoch millis). */
data class LatestSensorReading(val mmol: Float, val atMillis: Long)

class SensorReadingStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(bloodSugarMmol: Float, timestampMillis: Long) {
        prefs.edit()
            .putFloat(KEY_VALUE, bloodSugarMmol)
            .putLong(KEY_TIMESTAMP, timestampMillis)
            .apply()
    }

    /** The last reading however old it is - how fresh it counts as is [sensorFreshness]'s call. */
    fun latest(): LatestSensorReading? {
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)
        val value = prefs.getFloat(KEY_VALUE, -1f)
        if (timestamp == 0L || value <= 0f) return null
        return LatestSensorReading(value, timestamp)
    }

    companion object {
        private const val PREFS_NAME = "sensor_readings"
        private const val KEY_VALUE = "latest_mmol"
        private const val KEY_TIMESTAMP = "latest_timestamp"
    }
}
