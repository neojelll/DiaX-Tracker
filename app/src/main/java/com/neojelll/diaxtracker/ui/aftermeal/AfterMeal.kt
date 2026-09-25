package com.neojelll.diaxtracker.ui.aftermeal

import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.isMeal
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.math.roundToInt

/** Sugar readings 1-4 hours after a meal, as one pill on that meal's card. */
data class AfterMeal(
    /** Sugar at +1h..+4h; null where no reading was taken. Always 4 long. */
    val values: List<Double?>,
    /** How many of the four hours belong to this meal: fewer than 4 when the next meal cut the window. */
    val ownHours: Int,
    /** The meal that cut the window short, if any (its label may be null for a manual amount). */
    val cutByMeal: DiaryEntry?
)

class AfterMealIndex(
    /** Meal id -> its pill. Only meals at least an hour old. */
    val byMeal: Map<Long, AfterMeal>,
    /** Readings shown inside a pill, so the feed doesn't list them a second time. */
    val hidden: Set<Long>
)

private val FIRST_READING_OFFSET: Duration = Duration.ofMinutes(30)
private val LAST_READING_OFFSET: Duration = Duration.ofMinutes(270)
private val PILL_APPEARS_AFTER: Duration = Duration.ofHours(1)
private const val HOURS = 4

/**
 * Attaches sugar readings to the meals they follow. A reading belongs to the last meal before
 * it, when it comes 30 min to 4 h 30 min after that meal; it fills the cell of the nearest whole
 * hour (1..4). The window of a meal is cut where the next meal starts. Of several readings that
 * land in one cell the closest to the hour is shown, the rest stay ordinary rows.
 *
 * A meal gets its pill only once an hour has passed since it, and a reading disappears from the
 * feed only from that moment on - never before the pill it moves into exists. A meal followed by
 * another meal within the hour has no window of its own, so it gets no pill.
 */
fun buildAfterMeal(entries: List<DiaryEntry>, now: LocalDateTime): AfterMealIndex {
    val meals = entries.filter { it.isMeal() }.sortedBy { it.createdAt }
    val readings = entries.filter { !it.isMeal() && it.bloodSugar != null }.sortedBy { it.createdAt }

    val byMeal = HashMap<Long, AfterMeal>()
    val hidden = HashSet<Long>()

    meals.forEachIndexed { index, meal ->
        if (now < meal.createdAt.plus(PILL_APPEARS_AFTER)) return@forEachIndexed
        val next = meals.getOrNull(index + 1)?.takeIf { it.createdAt > meal.createdAt }
        val ownHours = if (next == null) HOURS
        else minOf(HOURS.toLong(), Duration.between(meal.createdAt, next.createdAt).toHours()).toInt()
        if (ownHours == 0) return@forEachIndexed

        val windowEnd = listOfNotNull(next?.createdAt, meal.createdAt.plus(LAST_READING_OFFSET)).min()
        val cells = HashMap<Int, DiaryEntry>()
        readings
            .filter { it.createdAt >= meal.createdAt.plus(FIRST_READING_OFFSET) && it.createdAt < windowEnd }
            .forEach { reading ->
                val minutes = Duration.between(meal.createdAt, reading.createdAt).toMinutes()
                val cell = (minutes / 60.0).roundToInt()
                if (cell !in 1..ownHours) return@forEach
                val current = cells[cell]
                if (current == null || offCenter(meal, reading, cell) < offCenter(meal, current, cell)) cells[cell] = reading
            }

        byMeal[meal.id] = AfterMeal(
            values = (1..HOURS).map { cell -> cells[cell]?.bloodSugar?.toString()?.toDouble() },
            ownHours = ownHours,
            cutByMeal = next.takeIf { ownHours < HOURS }
        )
        cells.values.forEach { hidden += it.id }
    }
    return AfterMealIndex(byMeal, hidden)
}

private fun offCenter(meal: DiaryEntry, reading: DiaryEntry, cell: Int): Long =
    abs(Duration.between(meal.createdAt, reading.createdAt).toMinutes() - cell * 60L)
