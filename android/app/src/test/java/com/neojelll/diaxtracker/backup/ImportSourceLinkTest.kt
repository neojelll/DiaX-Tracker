package com.neojelll.diaxtracker.backup

import com.neojelll.diaxtracker.data.DiaryEntry
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImportSourceLinkTest {
    private fun check(source: Long?) = DiaryEntry(
        id = 5, bloodSugar = 7f, shortInsulinDose = null, longInsulinDose = null, notes = "",
        createdAt = LocalDateTime.of(2026, 9, 25, 13, 0), sourceEntryId = source, sourceHour = source?.let { 1 }
    )

    @Test
    fun `the link follows the meal to its id in the live database`() {
        assertEquals(42L, check(source = 3).withSourceIn(mapOf(3L to 42L)).sourceEntryId)
    }

    @Test
    fun `a link to a meal that isn't in the archive is dropped`() {
        val unlinked = check(source = 3).withSourceIn(emptyMap())
        assertNull(unlinked.sourceEntryId)
        assertNull(unlinked.sourceHour)
    }

    @Test
    fun `an entry without a link is left alone`() {
        val plain = check(source = null)
        assertEquals(plain, plain.withSourceIn(mapOf(3L to 42L)))
    }

    @Test
    fun `a check imported twice is a duplicate only against the same meal`() {
        val existing = listOf(check(source = 42))
        val sameMeal = check(source = 3).withSourceIn(mapOf(3L to 42L))
        val otherMeal = check(source = 3).withSourceIn(mapOf(3L to 43L))
        assertEquals(existing.first(), findDuplicateEntry(existing, sameMeal.copy(id = 0)))
        assertNull(findDuplicateEntry(existing, otherMeal.copy(id = 0)))
    }
}
