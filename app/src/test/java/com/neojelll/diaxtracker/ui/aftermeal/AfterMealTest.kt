package com.neojelll.diaxtracker.ui.aftermeal

import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.SugarSource
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AfterMealTest {
    private val day = LocalDateTime.of(2026, 9, 25, 12, 0)
    private var nextId = 1L

    private fun minutes(m: Long) = day.plusMinutes(m)

    private fun meal(at: LocalDateTime, label: String? = "Обед") = DiaryEntry(
        id = nextId++, bloodSugar = null, shortInsulinDose = null, longInsulinDose = null,
        notes = "", mealLabel = label, breadUnits = if (label == null) 3f else null, createdAt = at
    )

    private fun check(of: DiaryEntry, hour: Int, sugar: Float, at: LocalDateTime = of.createdAt.plusHours(hour.toLong())) = DiaryEntry(
        id = nextId++, bloodSugar = sugar, sugarSource = SugarSource.SENSOR, shortInsulinDose = null, longInsulinDose = null,
        notes = "", createdAt = at, sourceEntryId = of.id, sourceHour = hour
    )

    private fun manualReading(at: LocalDateTime, sugar: Float) = DiaryEntry(
        id = nextId++, bloodSugar = sugar, shortInsulinDose = null, longInsulinDose = null, notes = "", createdAt = at
    )

    @Test
    fun `no pill within the first hour`() {
        val lunch = meal(day)
        val index = buildAfterMeal(listOf(lunch), now = minutes(59))
        assertTrue(index.byMeal.isEmpty())
    }

    @Test
    fun `the pill appears at one hour and fills up as the checks arrive`() {
        val lunch = meal(day)
        val first = check(lunch, 1, 8.4f)
        val atOneHour = buildAfterMeal(listOf(lunch, first), now = minutes(60))
        assertEquals(listOf(8.4, null, null, null), atOneHour.byMeal.getValue(lunch.id).values)
        assertEquals(setOf(first.id), atOneHour.hidden)

        val all = listOf(lunch, first, check(lunch, 2, 7.9f), check(lunch, 3, 7.1f), check(lunch, 4, 6.6f))
        assertEquals(listOf(8.4, 7.9, 7.1, 6.6), buildAfterMeal(all, now = minutes(250)).byMeal.getValue(lunch.id).values)
    }

    @Test
    fun `a pill with no checks yet still appears after an hour`() {
        val lunch = meal(day)
        assertEquals(listOf(null, null, null, null), buildAfterMeal(listOf(lunch), minutes(75)).byMeal.getValue(lunch.id).values)
    }

    @Test
    fun `two overlapping meals each keep their own four checks`() {
        val lunch = meal(day, "Обед")
        val dinner = meal(minutes(90), "Ужин")
        val entries = listOf(lunch, dinner) +
            (1..4).map { check(lunch, it, 6.0f + it) } +
            (1..4).map { check(dinner, it, 9.0f + it) }

        val index = buildAfterMeal(entries, now = minutes(600))
        assertEquals(listOf(7.0, 8.0, 9.0, 10.0), index.byMeal.getValue(lunch.id).values)
        assertEquals(listOf(10.0, 11.0, 12.0, 13.0), index.byMeal.getValue(dinner.id).values)
        assertEquals(8, index.hidden.size)
    }

    @Test
    fun `a check belongs to its meal even when it was stamped at an odd time`() {
        val lunch = meal(day)
        val late = check(lunch, 2, 7.5f, at = minutes(140))
        assertEquals(7.5, buildAfterMeal(listOf(lunch, late), minutes(300)).byMeal.getValue(lunch.id).values[1]!!, 0.0)
    }

    @Test
    fun `manual readings are not part of the pill and stay in the feed`() {
        val lunch = meal(day)
        val manual = manualReading(minutes(60), 8.0f)
        val index = buildAfterMeal(listOf(lunch, manual), now = minutes(120))
        assertEquals(listOf(null, null, null, null), index.byMeal.getValue(lunch.id).values)
        assertFalse(manual.id in index.hidden)
    }

    @Test
    fun `a check whose meal is gone stays an ordinary row`() {
        val gone = meal(day)
        val orphan = check(gone, 1, 8.0f)
        val index = buildAfterMeal(listOf(orphan), now = minutes(600))
        assertTrue(index.byMeal.isEmpty())
        assertFalse(orphan.id in index.hidden)
    }

    @Test
    fun `an entry that is no longer a meal has no pill`() {
        val notMeal = meal(day).copy(mealLabel = null, breadUnits = null)
        assertNull(buildAfterMeal(listOf(notMeal), minutes(300)).byMeal[notMeal.id])
    }
}
