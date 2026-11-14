package com.neojelll.diaxtracker.ui.components

import com.neojelll.diaxtracker.R
import org.junit.Assert.assertEquals
import org.junit.Test

class DaySummaryCardTest {
    private val minute = 60_000L

    @Test
    fun `a stale reading's age is in minutes, then hours, then days`() {
        assertEquals(R.string.summary_sensor_minutes_ago to 16L, sensorAge(16 * minute))
        assertEquals(R.string.summary_sensor_minutes_ago to 59L, sensorAge(59 * minute + 59_000))
        assertEquals(R.string.summary_sensor_hours_ago to 1L, sensorAge(60 * minute))
        assertEquals(R.string.summary_sensor_hours_ago to 23L, sensorAge(24 * 60 * minute - 1))
        assertEquals(R.string.summary_sensor_days_ago to 2L, sensorAge(2 * 24 * 60 * minute))
    }

    @Test
    fun `values drop a trailing zero decimal`() {
        assertEquals("7", formatSummaryValue(7f))
        assertEquals("%.1f".format(6.4f), formatSummaryValue(6.4f))
    }
}
