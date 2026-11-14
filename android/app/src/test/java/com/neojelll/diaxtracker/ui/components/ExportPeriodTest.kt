package com.neojelll.diaxtracker.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ExportPeriodTest {

    private val today = LocalDate.of(2026, 9, 24)

    @Test
    fun `a week reaches back six days, so it covers seven calendar days including today`() {
        assertEquals(LocalDateTime.of(2026, 9, 18, 0, 0), ExportPeriod.Week.startsAt(today))
    }

    @Test
    fun `a month reaches back twenty-nine days, so it covers thirty calendar days including today`() {
        assertEquals(LocalDateTime.of(2026, 8, 26, 0, 0), ExportPeriod.Month.startsAt(today))
    }

    @Test
    fun `all has no lower bound`() {
        assertNull(ExportPeriod.All.startsAt(today))
    }

    @Test
    fun `the window starts at midnight so an entry earlier that morning still counts`() {
        val from = ExportPeriod.Week.startsAt(today)!!

        assertFalse(LocalDateTime.of(2026, 9, 18, 0, 5).isBefore(from))
        assertTrue(LocalDateTime.of(2026, 9, 17, 23, 59).isBefore(from))
    }
}
