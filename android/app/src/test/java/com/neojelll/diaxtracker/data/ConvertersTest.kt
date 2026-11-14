package com.neojelll.diaxtracker.data

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `round-trips a valid timestamp`() {
        val value = LocalDateTime.of(2026, 9, 10, 12, 30)
        val serialized = converters.fromLocalDateTime(value)
        assertEquals(value, converters.toLocalDateTime(serialized))
    }

    @Test
    fun `null timestamp stays null`() {
        assertNull(converters.toLocalDateTime(null))
    }

    @Test
    fun `a corrupt timestamp does not throw and falls back to a real value`() {
        assertNotNull(converters.toLocalDateTime("not-a-date"))
    }

    @Test
    fun `round-trips a valid sugar source`() {
        assertEquals(SugarSource.SENSOR, converters.toSugarSource(converters.fromSugarSource(SugarSource.SENSOR)))
    }

    @Test
    fun `null sugar source stays null`() {
        assertNull(converters.toSugarSource(null))
    }

    @Test
    fun `an unknown sugar source does not throw and falls back to null`() {
        assertNull(converters.toSugarSource("NOT_A_REAL_SOURCE"))
    }
}
