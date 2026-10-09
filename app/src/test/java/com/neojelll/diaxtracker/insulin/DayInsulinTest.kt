package com.neojelll.diaxtracker.insulin

import com.neojelll.diaxtracker.data.DiaryEntry
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DayInsulinTest {
    private val day = LocalDate.of(2026, 10, 9)

    private fun entry(at: LocalDateTime, short: Float? = null, long: Float? = null) = DiaryEntry(
        bloodSugar = null, shortInsulinDose = short, longInsulinDose = long, notes = "", createdAt = at
    )

    @Test
    fun `no entries - no short insulin and no long line`() {
        val insulin = insulinOn(day, emptyList())
        assertEquals(0f, insulin.short, 1e-4f)
        assertNull(insulin.long)
    }

    @Test
    fun `short and long are summed separately, other days ignored`() {
        val entries = listOf(
            entry(day.atTime(8, 0), short = 4f, long = 10f),
            entry(day.atTime(13, 0), short = 3.5f),
            entry(day.atTime(21, 0), long = 10f),
            entry(day.minusDays(1).atTime(23, 59), short = 6f, long = 12f),
            entry(day.plusDays(1).atTime(0, 0), short = 2f)
        )
        val insulin = insulinOn(day, entries)
        assertEquals(7.5f, insulin.short, 1e-4f)
        assertEquals(20f, insulin.long!!, 1e-4f)
    }

    @Test
    fun `short only - the long line stays hidden`() {
        assertNull(insulinOn(day, listOf(entry(day.atTime(12, 0), short = 5f))).long)
    }
}
