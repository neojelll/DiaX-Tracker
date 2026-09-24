package com.neojelll.diaxtracker.backup

import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.SugarSource
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class IsDuplicateEntryTest {

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

        assertTrue(isDuplicateEntry(existing, candidate))
    }

    @Test
    fun `nothing logged at that moment means it is not a duplicate`() {
        assertFalse(isDuplicateEntry(emptyList(), entry()))
    }

    @Test
    fun `the same moment with different values is a conflict, so both are kept`() {
        val existing = listOf(entry(sugar = 6.5f))

        assertFalse(isDuplicateEntry(existing, entry(sugar = 9.1f)))
        assertFalse(isDuplicateEntry(existing, entry(insulin = 6f)))
        assertFalse(isDuplicateEntry(existing, entry(notes = "after a walk")))
    }

    @Test
    fun `identical values at another moment are not a duplicate`() {
        val existing = listOf(entry(createdAt = at))

        assertFalse(isDuplicateEntry(existing, entry(createdAt = at.plusMinutes(1))))
    }

    @Test
    fun `one matching record among several at that moment is enough`() {
        val existing = listOf(entry(sugar = 5.0f), entry(sugar = 6.5f), entry(sugar = 8.0f))

        assertTrue(isDuplicateEntry(existing, entry(sugar = 6.5f)))
    }
}
