package com.neojelll.diaxtracker.ui.aftermeal

import com.neojelll.diaxtracker.data.DiaryEntry
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AfterMealTest {
    private val day = LocalDateTime.of(2026, 9, 25, 12, 0)
    private var nextId = 1L

    private fun meal(at: LocalDateTime, label: String? = "Обед") = DiaryEntry(
        id = nextId++, bloodSugar = null, shortInsulinDose = null, longInsulinDose = null,
        notes = "", mealLabel = label, breadUnits = if (label == null) 3f else null, createdAt = at
    )

    private fun reading(at: LocalDateTime, sugar: Float) = DiaryEntry(
        id = nextId++, bloodSugar = sugar, shortInsulinDose = null, longInsulinDose = null,
        notes = "", createdAt = at
    )

    private fun minutes(m: Long) = day.plusMinutes(m)

    @Test
    fun `no pill within the first hour`() {
        val lunch = meal(day)
        val index = buildAfterMeal(listOf(lunch, reading(minutes(40), 7.0f)), now = minutes(59))
        assertTrue(index.byMeal.isEmpty())
        assertTrue(index.hidden.isEmpty())
    }

    @Test
    fun `the pill appears at one hour and starts with only the first cell filled`() {
        val lunch = meal(day)
        val first = reading(minutes(60), 8.4f)
        val index = buildAfterMeal(listOf(lunch, first), now = minutes(60))
        assertEquals(listOf(8.4, null, null, null), index.byMeal.getValue(lunch.id).values)
        assertEquals(setOf(first.id), index.hidden)
    }

    @Test
    fun `a pill with no readings yet still appears after an hour`() {
        val lunch = meal(day)
        val index = buildAfterMeal(listOf(lunch), now = minutes(75))
        assertEquals(listOf(null, null, null, null), index.byMeal.getValue(lunch.id).values)
    }

    @Test
    fun `it fills up as readings arrive`() {
        val lunch = meal(day)
        val readings = listOf(60L to 8.4f, 120L to 7.9f, 180L to 7.1f, 240L to 6.6f).map { reading(minutes(it.first), it.second) }
        val index = buildAfterMeal(listOf(lunch) + readings, now = minutes(250))
        assertEquals(listOf(8.4, 7.9, 7.1, 6.6), index.byMeal.getValue(lunch.id).values)
        assertEquals(readings.map { it.id }.toSet(), index.hidden)
    }

    @Test
    fun `a reading lands in the nearest whole hour and the window is 30 min to 4 h 30`() {
        val lunch = meal(day)
        val tooEarly = reading(minutes(29), 5.0f)
        val halfHour = reading(minutes(30), 6.0f)
        val late = reading(minutes(269), 7.0f)
        val tooLate = reading(minutes(271), 9.0f)
        val index = buildAfterMeal(listOf(lunch, tooEarly, halfHour, late, tooLate), now = minutes(300))
        assertEquals(listOf(6.0, null, null, 7.0), index.byMeal.getValue(lunch.id).values)
        assertFalse(tooEarly.id in index.hidden)
        assertFalse(tooLate.id in index.hidden)
    }

    @Test
    fun `of two readings in one cell the closer to the hour is shown and the other stays in the feed`() {
        val lunch = meal(day)
        val near = reading(minutes(120), 7.0f)
        val far = reading(minutes(135), 7.5f)
        val index = buildAfterMeal(listOf(lunch, far, near), now = minutes(200))
        assertEquals(7.0, index.byMeal.getValue(lunch.id).values[1]!!, 0.0)
        assertTrue(near.id in index.hidden)
        assertFalse(far.id in index.hidden)
    }

    @Test
    fun `the next meal cuts the window and takes the later readings`() {
        val lunch = meal(day, "Обед")
        val dinner = meal(minutes(150), "Ужин")
        val r1 = reading(minutes(60), 6.9f)
        val r2 = reading(minutes(120), 6.1f)
        val afterDinner = reading(minutes(210), 8.0f)
        val index = buildAfterMeal(listOf(lunch, dinner, r1, r2, afterDinner), now = minutes(400))

        val lunchPill = index.byMeal.getValue(lunch.id)
        assertEquals(2, lunchPill.ownHours)
        assertEquals(listOf(6.9, 6.1, null, null), lunchPill.values)
        assertEquals(dinner, lunchPill.cutByMeal)
        // 3 h 30 after lunch but 1 h after dinner: belongs to dinner.
        assertEquals(8.0, index.byMeal.getValue(dinner.id).values[0]!!, 0.0)
    }

    @Test
    fun `a meal followed by another within the hour gets no pill`() {
        val lunch = meal(day)
        val snack = meal(minutes(45), null)
        val index = buildAfterMeal(listOf(lunch, snack), now = minutes(300))
        assertNull(index.byMeal[lunch.id])
        assertTrue(index.byMeal.containsKey(snack.id))
    }

    @Test
    fun `entries with food never count as readings`() {
        val lunch = meal(day)
        val snack = meal(minutes(120), "Перекус").copy(bloodSugar = 6.0f)
        val index = buildAfterMeal(listOf(lunch, snack), now = minutes(300))
        assertFalse(snack.id in index.hidden)
    }
}
