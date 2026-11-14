package com.neojelll.diaxtracker.sensor

import org.junit.Assert.assertEquals
import org.junit.Test

class SensorFreshnessTest {
    private val now = 1_800_000_000_000L
    private val minute = 60_000L
    private fun readingAgo(millis: Long) = LatestSensorReading(6.4f, now - millis)

    @Test
    fun `no reading at all means no sensor`() {
        assertEquals(SensorFreshness.NONE, sensorFreshness(null, now))
    }

    @Test
    fun `fresh up to fifteen minutes, stale right after`() {
        assertEquals(SensorFreshness.FRESH, sensorFreshness(readingAgo(0), now))
        assertEquals(SensorFreshness.FRESH, sensorFreshness(readingAgo(15 * minute), now))
        assertEquals(SensorFreshness.STALE, sensorFreshness(readingAgo(15 * minute + 1), now))
    }

    @Test
    fun `stale up to three days, then no sensor`() {
        val threeDays = 3 * 24 * 60 * minute
        assertEquals(SensorFreshness.STALE, sensorFreshness(readingAgo(threeDays), now))
        assertEquals(SensorFreshness.NONE, sensorFreshness(readingAgo(threeDays + 1), now))
    }
}
