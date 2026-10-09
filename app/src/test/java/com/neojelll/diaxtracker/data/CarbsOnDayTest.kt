package com.neojelll.diaxtracker.data

import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class CarbsOnDayTest {
    private val day = LocalDate.of(2026, 10, 9)
    private var nextId = 1L

    private fun entry(at: LocalDateTime, carbsGrams: Float?) = DiaryEntry(
        id = nextId++, bloodSugar = null, shortInsulinDose = null, longInsulinDose = null,
        notes = "", carbsGrams = carbsGrams, createdAt = at
    )

    private fun product(of: DiaryEntry, grams: Float) =
        DiaryEntryProduct(diaryEntryId = of.id, name = "x", carbsGrams = grams, sortOrder = 0)

    @Test
    fun `sums the day's entries and ignores other days`() {
        val entries = listOf(
            entry(day.atTime(8, 0), 30f),
            entry(day.atTime(13, 0), 45f),
            entry(day.atTime(23, 59), 10f),
            entry(day.minusDays(1).atTime(23, 59), 50f),
            entry(day.plusDays(1).atTime(0, 0), 20f)
        )
        assertEquals(85f, carbsGramsOn(day, entries, emptyList()), 1e-4f)
    }

    @Test
    fun `an entry's own total wins over its products, which count only when it has none`() {
        val withTotal = entry(day.atTime(9, 0), 40f)
        val olderWithoutTotal = entry(day.atTime(12, 0), null)
        val products = listOf(product(withTotal, 15f), product(olderWithoutTotal, 12f), product(olderWithoutTotal, 8f))
        assertEquals(60f, carbsGramsOn(day, listOf(withTotal, olderWithoutTotal), products), 1e-4f)
    }

    @Test
    fun `entries without carbs add nothing`() {
        assertEquals(0f, carbsGramsOn(day, listOf(entry(day.atTime(10, 0), null)), emptyList()), 1e-4f)
    }
}
