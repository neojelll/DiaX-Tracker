package com.neojelll.diaxtracker.ui.notifications

import com.neojelll.diaxtracker.data.DiaryEntry
import java.time.LocalDateTime

/** How far either side of "this time yesterday" a meal still counts as at the same time. */
internal const val YESTERDAY_MEAL_WINDOW_MINUTES = 60L

/** Food was logged: a preset or a manual amount. Sugar checks and insulin-only entries aren't meals. */
internal fun DiaryEntry.isMeal(): Boolean = mealLabel != null || breadUnits != null

/** Yesterday's meals within the window around this moment, earliest first. */
internal fun yesterdayMeals(entries: List<DiaryEntry>, now: LocalDateTime): List<DiaryEntry> {
    val center = now.minusDays(1)
    val from = center.minusMinutes(YESTERDAY_MEAL_WINDOW_MINUTES)
    val to = center.plusMinutes(YESTERDAY_MEAL_WINDOW_MINUTES)
    return entries.filter { it.isMeal() && it.createdAt in from..to }.sortedBy { it.createdAt }
}
