package com.neojelll.diaxtracker.data

import android.content.Context

/** The daily carbohydrate goal, in grams. Off and unset until the person sets it in Settings. */
class CarbGoalStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(): CarbGoal = CarbGoal(
        enabled = prefs.getBoolean(KEY_ENABLED, false),
        grams = if (prefs.contains(KEY_GRAMS)) prefs.getFloat(KEY_GRAMS, 0f).coerceIn(0f, MAX_CARB_GOAL_GRAMS) else null
    )

    fun save(goal: CarbGoal) {
        val editor = prefs.edit().putBoolean(KEY_ENABLED, goal.enabled)
        if (goal.grams != null) editor.putFloat(KEY_GRAMS, goal.grams) else editor.remove(KEY_GRAMS)
        editor.apply()
    }

    companion object {
        private const val PREFS_NAME = "carb_goal"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_GRAMS = "grams"
    }
}
