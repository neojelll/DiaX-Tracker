package com.neojelll.diaxtracker.ui.aftermeal

import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.isMeal
import java.time.Duration
import java.time.LocalDateTime

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
 */
fun buildAfterMeal(entries: List<DiaryEntry>, now: LocalDateTime): AfterMealIndex {
    val checksByMeal = entries
        .filter { it.sourceEntryId != null && it.sourceHour in 1..HOURS && it.bloodSugar != null }
        .groupBy { it.sourceEntryId!! }

    val byMeal = HashMap<Long, AfterMeal>()
    val hidden = HashSet<Long>()

    entries.filter { it.isMeal() && now >= it.createdAt.plus(PILL_APPEARS_AFTER) }.forEach { meal ->
        val checks = checksByMeal[meal.id].orEmpty()
        // Two checks for the same hour can only come from a meal edited and rescheduled: the later wins.
        val byHour = checks.groupBy { it.sourceHour!! }.mapValues { (_, same) -> same.maxBy { it.createdAt } }
        byMeal[meal.id] = AfterMeal((1..HOURS).map { hour -> byHour[hour]?.bloodSugar?.toString()?.toDouble() })
        hidden += checks.map { it.id }
    }
    return AfterMealIndex(byMeal, hidden)
}
