package com.neojelll.diaxtracker.backup

import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.SugarSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import java.time.LocalDateTime

class FindDuplicateEntryTest {

    private val at = LocalDateTime.of(2026, 1, 1, 12, 30)

    private fun entry(
        id: Long = 0,
        createdAt: LocalDateTime = at,
        sugar: Float? = 6.5f,
        insulin: Float? = 4f,
        notes: String = "",
        photoPath: String? = null
    ) = DiaryEntry(
        id = id,
        bloodSugar = sugar,
        sugarSource = SugarSource.MANUAL,
        shortInsulinDose = insulin,
        longInsulinDose = null,
        notes = notes,
        photoPath = photoPath,
        createdAt = createdAt
    )

    @Test
    fun `the same record is a duplicate even with a different id and photo path`() {
        val existing = listOf(entry(id = 7, photoPath = "/data/user/0/app/files/entry_photos/a.jpg"))
        val candidate = entry(id = 300, photoPath = "/somewhere/else/b.jpg")

        assertSame(existing.single(), findDuplicateEntry(existing, candidate))
    }

    @Test
    fun `nothing logged at that moment means it is not a duplicate`() {
        assertNull(findDuplicateEntry(emptyList(), entry()))
    }

    @Test
    fun `the same moment with different values is a conflict, so both are kept`() {
        val existing = listOf(entry(sugar = 6.5f))

        assertNull(findDuplicateEntry(existing, entry(sugar = 9.1f)))
        assertNull(findDuplicateEntry(existing, entry(insulin = 6f)))
        assertNull(findDuplicateEntry(existing, entry(notes = "after a walk")))
    }

    @Test
    fun `identical values at another moment are not a duplicate`() {
        val existing = listOf(entry(createdAt = at))

        assertNull(findDuplicateEntry(existing, entry(createdAt = at.plusMinutes(1))))
    }

    @Test
    fun `the match returned is the one with the same values among several at that moment`() {
        val existing = listOf(entry(id = 1, sugar = 5.0f), entry(id = 2, sugar = 6.5f), entry(id = 3, sugar = 8.0f))

        assertEquals(2L, findDuplicateEntry(existing, entry(sugar = 6.5f))!!.id)
    }
}
