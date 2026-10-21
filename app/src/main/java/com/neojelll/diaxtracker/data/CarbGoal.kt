package com.neojelll.diaxtracker.data

/**
 * A personal daily carbohydrate goal: something to reach, not a limit - going past it is fine.
 * [grams] is null until the person types a number; there is no default on purpose, since any
 * preset value would read as a recommendation.
 */
data class CarbGoal(val enabled: Boolean = false, val grams: Float? = null) {
    /** The goal in effect: switched on and set to more than zero. */
    val activeGrams: Float?
        get() = grams?.takeIf { enabled && it > 0f }
}

/** Upper bound for a typed goal - guards against a stray extra digit, not an editorial limit. */
const val MAX_CARB_GOAL_GRAMS = 1000f

/** How far [eatenGrams] is toward [goalGrams], 0..1; past the goal stays full. */
fun carbGoalProgress(eatenGrams: Float, goalGrams: Float): Float =
    (eatenGrams / goalGrams).coerceIn(0f, 1f)

fun carbGoalReached(eatenGrams: Float, goalGrams: Float): Boolean = eatenGrams >= goalGrams

/** One −/+ step in grams: half an XE when carbs are shown in XE, 5 g when in grams. */
fun carbGoalStepGrams(display: CarbDisplay): Float = when (display.unit) {
    CarbUnit.XE -> xeToGrams(0.5f, display.gramsPerXe)
    CarbUnit.GRAMS -> 5f
}

/** [grams] moved by [steps] −/+ steps, kept within 0..[MAX_CARB_GOAL_GRAMS]; from "not set" it starts at 0. */
fun stepCarbGoal(grams: Float?, steps: Int, display: CarbDisplay): Float =
    ((grams ?: 0f) + steps * carbGoalStepGrams(display)).coerceIn(0f, MAX_CARB_GOAL_GRAMS)
