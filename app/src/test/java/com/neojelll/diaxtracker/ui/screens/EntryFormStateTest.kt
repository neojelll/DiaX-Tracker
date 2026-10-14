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

        val live = picked.followingClock(later)
        assertEquals(false, live.isPinned)
        assertEquals(later, live.dateTimeAt(later))
    }

    @Test
    fun `entering something holds the time shown, instead of following the clock`() {
        val form = EntryFormState(bloodSugar = "6.4").held(now = opened)
        assertEquals(true, form.isPinned)
        assertEquals(opened, form.dateTimeAt(later))
        // Held once - later edits don't move it.
        assertEquals(opened, form.copy(notes = "run").held(now = later).dateTimeAt(later.plusMinutes(5)))
    }

    @Test
    fun `an emptied form follows the clock again`() {
        val emptied = EntryFormState(bloodSugar = "6.4").held(now = opened).copy(bloodSugar = "").held(now = later)
        assertEquals(false, emptied.isPinned)
        assertEquals(later, emptied.dateTimeAt(later))
    }

    @Test
    fun `a picked moment wins over the held one`() {
        val form = EntryFormState(bloodSugar = "6.4").held(now = opened).withTime(LocalTime.of(9, 30), now = later)
        assertEquals(LocalDateTime.of(2026, 10, 9, 9, 30), form.dateTimeAt(later))
        assertEquals(form, form.held(now = later.plusMinutes(1)))
    }

    @Test
    fun `back to now on a filled form holds the new now`() {
        val form = EntryFormState(bloodSugar = "6.4").withTime(LocalTime.of(9, 30), now = opened)
            .followingClock(later)
        assertEquals(later, form.dateTimeAt(later.plusMinutes(5)))
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

    @Test
    fun `clearing drops a held moment - the form follows the clock again`() {
        val cleared = EntryFormState(bloodSugar = "6.4").held(now = opened).cleared()
        assertEquals(later, cleared.dateTimeAt(later))
    }
}
