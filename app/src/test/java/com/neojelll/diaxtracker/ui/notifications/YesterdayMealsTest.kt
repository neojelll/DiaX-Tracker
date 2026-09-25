package com.neojelll.diaxtracker.ui.notifications

import com.neojelll.diaxtracker.data.DiaryEntry
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class YesterdayMealsTest {
    private val now = LocalDateTime.of(2026, 9, 25, 13, 0)

    private fun entry(at: LocalDateTime, label: String? = null, xe: Float? = null, sugar: Float? = null) =
        DiaryEntry(
            id = at.hashCode().toLong(), bloodSugar = sugar, shortInsulinDose = null, longInsulinDose = null,
            notes = "", mealLabel = label, breadUnits = xe, createdAt = at
        )

    @Test
    fun `a meal at the same time yesterday is included`() {
        val meal = entry(LocalDateTime.of(2026, 9, 24, 13, 20), label = "Обед")
        assertEquals(listOf(meal), yesterdayMeals(listOf(meal), now))
    }

    @Test
    fun `the window is one hour either side and inclusive`() {
        val early = entry(LocalDateTime.of(2026, 9, 24, 12, 0), xe = 3f)
        val late = entry(LocalDateTime.of(2026, 9, 24, 14, 0), xe = 3f)
        val tooEarly = entry(LocalDateTime.of(2026, 9, 24, 11, 59), xe = 3f)
        val tooLate = entry(LocalDateTime.of(2026, 9, 24, 14, 1), xe = 3f)
        assertEquals(listOf(early, late), yesterdayMeals(listOf(tooLate, late, tooEarly, early), now))
    }

    @Test
    fun `entries without food do not trigger it`() {
        val sugarOnly = entry(LocalDateTime.of(2026, 9, 24, 13, 0), sugar = 6.1f)
        assertEquals(emptyList<DiaryEntry>(), yesterdayMeals(listOf(sugarOnly), now))
    }

    @Test
    fun `other days do not count`() {
        val today = entry(LocalDateTime.of(2026, 9, 25, 13, 0), label = "Обед")
        val twoDaysAgo = entry(LocalDateTime.of(2026, 9, 23, 13, 0), label = "Обед")
        assertEquals(emptyList<DiaryEntry>(), yesterdayMeals(listOf(today, twoDaysAgo), now))
    }

    @Test
    fun `the window crosses midnight`() {
        val lateNow = LocalDateTime.of(2026, 9, 25, 0, 30)
        val meal = entry(LocalDateTime.of(2026, 9, 23, 23, 45), label = "Ужин")
        assertEquals(listOf(meal), yesterdayMeals(listOf(meal), lateNow))
    }
}
