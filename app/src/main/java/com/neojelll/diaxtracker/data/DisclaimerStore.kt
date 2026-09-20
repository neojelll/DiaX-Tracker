package com.neojelll.diaxtracker.data

import android.content.Context

class DisclaimerStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isAccepted(): Boolean = prefs.getBoolean(KEY_ACCEPTED, false)

    fun setAccepted() {
        prefs.edit().putBoolean(KEY_ACCEPTED, true).apply()
    }

    private companion object {
        const val PREFS_NAME = "disclaimer"
        const val KEY_ACCEPTED = "accepted"
    }
}
