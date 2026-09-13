package com.neojelll.diaxtracker.data

import android.content.Context

/**
 * Persists the decorative "backup every evening" toggle from Settings. No WorkManager job is
 * ever scheduled from this value — real backups stay manual export/import only.
 */
class BackupPreferencesStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isAutoBackupEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    fun setAutoBackupEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    companion object {
        private const val PREFS_NAME = "backup_preferences"
        private const val KEY_ENABLED = "auto_backup_enabled"
    }
}
