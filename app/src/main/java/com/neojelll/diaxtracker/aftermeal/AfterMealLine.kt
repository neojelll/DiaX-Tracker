package com.neojelll.diaxtracker.aftermeal

import java.time.LocalDateTime
import java.time.LocalTime

/**
 * One point of the after-meal line: sugar before the meal ([hour] 0) or at +1h..+4h. A missing
 * reading has [value] null and, while its check is still ahead, the time it's due at.
 */
data class AfterMealPoint(val hour: Int, val value: Double?, val dueAt: LocalTime?)

/**
 * What the after-meal line shows: the sugar logged with the meal as the starting point (when there
 * is one), then the four checks. A check not in yet shows when it's due; one whose time has passed
 * without a reading (no sensor data then) shows nothing.
 */
fun afterMealPoints(before: Double?, afterMeal: AfterMeal, mealAt: LocalDateTime, now: LocalDateTime): List<AfterMealPoint> =
    listOf(AfterMealPoint(0, before, null)) + afterMeal.values.mapIndexed { i, value ->
        val hour = i + 1
        val due = mealAt.plusHours(hour.toLong())
        AfterMealPoint(hour, value, if (value == null && due.isAfter(now)) due.toLocalTime() else null)
    }

/** Index of the highest check after the meal - the starting point doesn't count; null with none in. */
fun afterMealPeak(points: List<AfterMealPoint>): Int? =
    points.withIndex()
        .filter { (_, p) -> p.hour > 0 && p.value != null }
        .maxByOrNull { (_, p) -> p.value!! }
        ?.index
