package com.neojelll.diaxtracker.ui.screens

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class EntryFormStateTest {
    private val opened = LocalDateTime.of(2026, 10, 9, 12, 0)
    private val later = opened.plusMinutes(7)

    @Test
    fun `a new form follows the clock until a date or time is picked`() {
        val form = EntryFormState()
        assertEquals(opened, form.dateTimeAt(opened))
        assertEquals(later, form.dateTimeAt(later))
    }

    @Test
    fun `picking the time pins the moment, on the date shown when picking`() {
        val form = EntryFormState().withTime(LocalTime.of(9, 30), now = opened)
        assertEquals(LocalDateTime.of(2026, 10, 9, 9, 30), form.dateTimeAt(later))
    }

    @Test
    fun `picking the date pins the moment, at the time shown when picking`() {
        val form = EntryFormState().withDate(LocalDate.of(2026, 10, 8), now = opened)
        assertEquals(LocalDateTime.of(2026, 10, 8, 12, 0), form.dateTimeAt(later))
    }

    @Test
    fun `picking one part keeps the other part picked before`() {
        val form = EntryFormState()
            .withDate(LocalDate.of(2026, 10, 8), now = opened)
            .withTime(LocalTime.of(22, 15), now = later)
        assertEquals(LocalDateTime.of(2026, 10, 8, 22, 15), form.dateTimeAt(later.plusHours(1)))
    }

    @Test
    fun `back to now after picking follows the clock again`() {
        val picked = EntryFormState().withTime(LocalTime.of(9, 30), now = opened)
        assertEquals(true, picked.isPinned)

        val live = picked.followingClock()
        assertEquals(false, live.isPinned)
        assertEquals(later, live.dateTimeAt(later))
    }

    @Test
    fun `the form is dirty once anything is in it`() {
        assertEquals(false, EntryFormState().isDirty)
        assertEquals(true, EntryFormState(bloodSugar = "6.4").isDirty)
        assertEquals(true, EntryFormState(notes = "run").isDirty)
        assertEquals(true, EntryFormState(photoPath = "/p.jpg").isDirty)
        assertEquals(true, EntryFormState(presetId = 3L).isDirty)
        // Only a picked date/time, nothing entered: nothing to clear.
        assertEquals(false, EntryFormState().withTime(LocalTime.of(9, 30), now = opened).isDirty)
    }

    @Test
    fun `clearing keeps a picked date and time, and drops everything else`() {
        val filled = EntryFormState(bloodSugar = "6.4", notes = "run", photoPath = "/p.jpg", presetId = 3L)
            .withTime(LocalTime.of(9, 30), now = opened)
        val cleared = filled.cleared()
        assertEquals(false, cleared.isDirty)
        assertEquals(LocalDateTime.of(2026, 10, 9, 9, 30), cleared.dateTimeAt(later))
    }
}
