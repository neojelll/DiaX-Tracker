package com.neojelll.diaxtracker.ui.screens

import com.neojelll.diaxtracker.sensor.FRESH_FOR_MILLIS
import com.neojelll.diaxtracker.sensor.LatestSensorReading
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SensorSugarFillTest {

    private val now = 1_000_000_000L
    private val fresh = LatestSensorReading(mmol = 6.4f, atMillis = now - 60_000)

    @Test
    fun `a fresh reading is offered for an empty form following the clock`() {
        assertEquals(fresh, sensorSugarToFill(EntryFormState(), fresh, now))
    }

    @Test
    fun `nothing is offered without a reading`() {
        assertNull(sensorSugarToFill(EntryFormState(), null, now))
    }

    @Test
    fun `a stale reading is not offered`() {
        val stale = fresh.copy(atMillis = now - FRESH_FOR_MILLIS - 1)
        assertNull(sensorSugarToFill(EntryFormState(), stale, now))
    }

    @Test
    fun `nothing is offered once sugar is typed`() {
        assertNull(sensorSugarToFill(EntryFormState(bloodSugar = "5"), fresh, now))
    }

    @Test
    fun `nothing is offered for a picked moment`() {
        val pinned = EntryFormState(dateTime = LocalDateTime.of(2026, 10, 14, 9, 0))
        assertNull(sensorSugarToFill(pinned, fresh, now))
    }

    @Test
    fun `the field gets one decimal with a dot`() {
        assertEquals("6.4", sugarFieldText(6.4f))
        assertEquals("7.0", sugarFieldText(7f))
        assertEquals("5.6", sugarFieldText(101.0f / 18.0182f))
    }
}
