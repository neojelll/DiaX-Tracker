package com.neojelll.diaxtracker.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

class AutoBackupTest {

    @Test
    fun `the file name carries the date and time so names sort chronologically`() {
        assertEquals("diax-auto-2026-09-25-2103.zip", autoBackupFileName(LocalDateTime.of(2026, 9, 25, 21, 3, 44)))
    }

    private fun names(vararg days: Int) = days.map { "diax-auto-2026-09-%02d-2100.zip".format(it) }

    @Test
    fun `nothing is deleted while there are no more than seven`() {
        assertTrue(staleAutoBackups(names(19, 20, 21, 22, 23, 24, 25)).isEmpty())
    }

    @Test
    fun `the oldest beyond the newest seven are the ones deleted`() {
        val stale = staleAutoBackups(names(17, 18, 19, 20, 21, 22, 23, 24, 25))

        assertEquals(names(17, 18).sorted(), stale.sorted())
    }

    @Test
    fun `order in the folder listing does not matter`() {
        val shuffled = names(25, 17, 22, 19, 24, 18, 21, 23, 20)

        assertEquals(names(17, 18).sorted(), staleAutoBackups(shuffled).sorted())
    }

    @Test
    fun `only files named like ours are ever candidates`() {
        val others = listOf(
            "diax-backup-week-2026-09-24.zip",
            "diax-backup-2026-09-24.zip",
            "diax-auto-notes.zip",
            "photo.jpg",
            "diax-auto-2026-09-01-2100.zip.bak"
        )

        assertTrue(staleAutoBackups(others + names(19, 20, 21, 22, 23, 24, 25)).isEmpty())
        assertTrue(staleAutoBackups(others, keep = 0).isEmpty())
    }

    @Test
    fun `the window slides across a month boundary`() {
        val stale = staleAutoBackups(
            listOf("diax-auto-2026-08-30-2100.zip", "diax-auto-2026-09-01-2100.zip", "diax-auto-2026-09-02-2100.zip"),
            keep = 2
        )

        assertEquals(listOf("diax-auto-2026-08-30-2100.zip"), stale)
    }

    @Test
    fun `before the evening hour the next run is later today`() {
        assertEquals(Duration.ofHours(6), delayUntilNextEvening(LocalDateTime.of(2026, 9, 25, 15, 0)))
    }

    @Test
    fun `after the evening hour the next run is tomorrow`() {
        assertEquals(Duration.ofMinutes(23 * 60 + 30), delayUntilNextEvening(LocalDateTime.of(2026, 9, 25, 21, 30)))
    }

    @Test
    fun `exactly at the evening hour the next run is tomorrow, not now`() {
        assertEquals(Duration.ofHours(24), delayUntilNextEvening(LocalDateTime.of(2026, 9, 25, 21, 0)))
    }

    @Test
    fun `just after midnight the wait is about twenty-one hours`() {
        assertEquals(Duration.ofMinutes(20 * 60 + 50), delayUntilNextEvening(LocalDateTime.of(2026, 9, 26, 0, 10)))
    }
}
