package com.neojelll.diaxtracker.sensor

/** How current the last sensor reading is, from the diary's point of view. */
enum class SensorFreshness {
    /** Arrived within [FRESH_FOR_MILLIS]: readings are flowing. */
    FRESH,

    /** Older than that but within [ACTIVE_WITHIN_MILLIS]: a sensor was in use and has dropped out. */
    STALE,

    /** Nothing for longer, or never: no sensor in use as far as the diary can tell. */
    NONE
}

const val FRESH_FOR_MILLIS = 15 * 60 * 1000L
const val ACTIVE_WITHIN_MILLIS = 3 * 24 * 60 * 60 * 1000L

/**
 * The diary only sees whether readings arrive from Juggluco/xDrip - not the sensor itself - so this
 * is about the age of the last arrival, not a claim that the sensor is broken.
 */
fun sensorFreshness(reading: LatestSensorReading?, nowMillis: Long): SensorFreshness {
    if (reading == null) return SensorFreshness.NONE
    val age = nowMillis - reading.atMillis
    return when {
        age <= FRESH_FOR_MILLIS -> SensorFreshness.FRESH
        age <= ACTIVE_WITHIN_MILLIS -> SensorFreshness.STALE
        else -> SensorFreshness.NONE
    }
}
