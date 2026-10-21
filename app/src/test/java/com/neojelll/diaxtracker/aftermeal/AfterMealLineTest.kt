package com.neojelll.diaxtracker.aftermeal

import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AfterMealLineTest {

    private val meal = LocalDateTime.of(2026, 10, 21, 14, 10)

    @Test
    fun `the line starts at the sugar logged with the meal`() {
        val points = afterMealPoints(5.9, AfterMeal(listOf(8.2, 9.4, 7.6, 6.3)), meal, meal.plusHours(5))
        assertEquals(listOf(0, 1, 2, 3, 4), points.map { it.hour })
        assertEquals(listOf(5.9, 8.2, 9.4, 7.6, 6.3), points.map { it.value })
        assertEquals(2, afterMealPeak(points))
    }

    @Test
    fun `a check still ahead shows when it is due`() {
        val points = afterMealPoints(5.9, AfterMeal(listOf(8.2, 9.4, null, null)), meal, meal.plusMinutes(150))
        assertEquals(LocalTime.of(17, 10), points[3].dueAt)
        assertEquals(LocalTime.of(18, 10), points[4].dueAt)
        assertNull(points[1].dueAt)
    }

    @Test
    fun `a check whose time passed without a reading shows nothing`() {
        val points = afterMealPoints(5.9, AfterMeal(listOf(8.2, null, 7.6, 6.3)), meal, meal.plusHours(5))
        assertNull(points[2].value)
        assertNull(points[2].dueAt)
    }

    @Test
    fun `the starting point is never the peak`() {
        val points = afterMealPoints(12.0, AfterMeal(listOf(8.2, 7.4, null, null)), meal, meal.plusMinutes(150))
        assertEquals(1, afterMealPeak(points))
        assertNull(afterMealPeak(afterMealPoints(6.0, AfterMeal(listOf(null, null, null, null)), meal, meal.plusMinutes(70))))
    }
}
