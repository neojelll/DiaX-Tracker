package com.neojelll.diaxtracker.sensor

import com.neojelll.diaxtracker.data.SensorReadingLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class NearestSensorReadingTest {

    private val reference = LocalDateTime.of(2026, 1, 1, 15, 0)

    private fun reading(minutesFromReference: Long, sugar: Float) =
        SensorReadingLog(timestamp = reference.plusMinutes(minutesFromReference), bloodSugar = sugar)

    @Test
    fun `no readings means no match`() {
        assertNull(nearestSensorReading(emptyList(), reference))
    }

    @Test
    fun `picks the reading closest to the reference time`() {
        val readings = listOf(reading(-4, 5.0f), reading(-1, 6.0f), reading(3, 7.0f))

        assertEquals(6.0f, nearestSensorReading(readings, reference)!!.bloodSugar, 0f)
    }

    @Test
    fun `a reading after the reference can be the nearest`() {
        val readings = listOf(reading(-4, 5.0f), reading(2, 7.0f))

        assertEquals(7.0f, nearestSensorReading(readings, reference)!!.bloodSugar, 0f)
    }

    @Test
    fun `an exact hit wins over near misses`() {
        val readings = listOf(reading(-1, 5.0f), reading(0, 6.5f), reading(1, 7.0f))

        assertEquals(6.5f, nearestSensorReading(readings, reference)!!.bloodSugar, 0f)
    }

    @Test
    fun `sub-minute differences still decide the winner`() {
        val slightlyBefore = SensorReadingLog(timestamp = reference.minusSeconds(50), bloodSugar = 5.0f)
        val slightlyAfter = SensorReadingLog(timestamp = reference.plusSeconds(20), bloodSugar = 6.0f)

        assertEquals(6.0f, nearestSensorReading(listOf(slightlyBefore, slightlyAfter), reference)!!.bloodSugar, 0f)
    }
}
