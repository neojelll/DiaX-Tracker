package com.neojelll.diaxtracker.ui.aftermeal

import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.SugarSource
import com.neojelll.diaxtracker.data.isMeal
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.math.roundToInt

/** The four automatic sugar checks of one meal, +1h..+4h after it, as one pill on its card. */
data class AfterMeal(
    /** Sugar at +1h..+4h; null where that check hasn't happened (yet) or found no sensor reading. Always 4 long. */
    val values: List<Double?>
)

class AfterMealIndex(
    /** Meal id -> its pill. Only meals at least an hour old. */
    val byMeal: Map<Long, AfterMeal>,
    /** Checks shown inside a pill, so the feed doesn't list them a second time. */
    val hidden: Set<Long>
)

private val PILL_APPEARS_AFTER: Duration = Duration.ofHours(1)
private val LEGACY_FIRST_OFFSET: Duration = Duration.ofMinutes(30)
private val LEGACY_LAST_OFFSET: Duration = Duration.ofMinutes(270)
private const val HOURS = 4

/**
 * Each meal owns its own checks: an automatic check records which meal scheduled it and for which
 * hour, so a meal's pill is exactly its own four checks, whatever else was logged around it -
 * another meal an hour later gets its own, independent set. Manual readings are not part of it.
 *
 * A meal gets its pill an hour after it; the checks appear in the pill as they are taken and
 * vanish from the feed at that point (a check exists only once its hour has passed, so it never
 * leaves the feed before the pill it moves into). A check whose meal is gone, or is no longer a
 * meal, stays an ordinary row.
 *
 * Checks recorded before the link existed (or imported from an archive that predates it) carry no
 * meal; see [legacyChecks] for how those are still placed.
 */
fun buildAfterMeal(entries: List<DiaryEntry>, now: LocalDateTime): AfterMealIndex {
    val checksByMeal = entries
        .filter { it.sourceEntryId != null && it.sourceHour in 1..HOURS && it.bloodSugar != null }
        .groupBy { it.sourceEntryId!! }

    val byMeal = HashMap<Long, AfterMeal>()
    val hidden = HashSet<Long>()

    val meals = entries.filter { it.isMeal() }
    val legacyByMeal = legacyChecks(entries, meals)

    meals.filter { now >= it.createdAt.plus(PILL_APPEARS_AFTER) }.forEach { meal ->
        val checks = checksByMeal[meal.id].orEmpty()
        // Two checks for the same hour can only come from a meal edited and rescheduled: the later wins.
        val byHour = checks.groupBy { it.sourceHour!! }.mapValues { (_, same) -> same.maxBy { it.createdAt } }
        val legacy = legacyByMeal[meal.id].orEmpty().filterKeys { it !in byHour }
        byMeal[meal.id] = AfterMeal(
            (1..HOURS).map { hour -> (byHour[hour] ?: legacy[hour])?.bloodSugar?.toString()?.toDouble() }
        )
        hidden += checks.map { it.id }
        hidden += legacy.values.map { it.id }
    }
    return AfterMealIndex(byMeal, hidden)
}

/**
 * Old automatic checks have no stored meal. They are recognised as sensor readings with nothing
 * else in them (no food, insulin or note) and given to the last meal before them, when they come
 * 30 min to 4 h 30 after it, in the cell of the nearest whole hour (the closest to the hour if
 * several). Returns meal id -> hour -> entry.
 */
private fun legacyChecks(entries: List<DiaryEntry>, meals: List<DiaryEntry>): Map<Long, Map<Int, DiaryEntry>> {
    val sortedMeals = meals.sortedBy { it.createdAt }
    val result = HashMap<Long, HashMap<Int, DiaryEntry>>()
    entries
        .filter {
            it.sourceEntryId == null && it.sugarSource == SugarSource.SENSOR && it.bloodSugar != null &&
                !it.isMeal() && it.shortInsulinDose == null && it.longInsulinDose == null && it.notes.isBlank()
        }
        .forEach { reading ->
            val meal = sortedMeals.lastOrNull { it.createdAt <= reading.createdAt.minus(LEGACY_FIRST_OFFSET) } ?: return@forEach
            val elapsed = Duration.between(meal.createdAt, reading.createdAt)
            if (elapsed >= LEGACY_LAST_OFFSET) return@forEach
            val hour = (elapsed.toMinutes() / 60.0).roundToInt()
            if (hour !in 1..HOURS) return@forEach
            val cells = result.getOrPut(meal.id) { HashMap() }
            val current = cells[hour]
            if (current == null || offCenter(meal, reading, hour) < offCenter(meal, current, hour)) cells[hour] = reading
        }
    return result
}

private fun offCenter(meal: DiaryEntry, reading: DiaryEntry, hour: Int): Long =
    abs(Duration.between(meal.createdAt, reading.createdAt).toMinutes() - hour * 60L)
