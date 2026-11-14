package com.neojelll.diaxtracker.ui.components

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class ToastTest {
    private val today = LocalDate.of(2026, 9, 25)

    @Test
    fun `today's moment uses the today format`() {
        val at = LocalDateTime.of(2026, 9, 25, 9, 41)
        assertEquals("Today, 09:41", toastMoment(at, today, "Today, %1\$s", Locale.US))
    }

    @Test
    fun `other days show date and time`() {
        val at = LocalDateTime.of(2026, 9, 10, 13, 20)
        assertEquals("10 Sep, 13:20", toastMoment(at, today, "Today, %1\$s", Locale.US))
    }

    @Test
    fun `undo lengthens the toast`() {
        assertEquals(TOAST_MS, ToastData("a").durationMs)
        assertEquals(TOAST_WITH_UNDO_MS, ToastData("a", onUndo = {}).durationMs)
    }
}
