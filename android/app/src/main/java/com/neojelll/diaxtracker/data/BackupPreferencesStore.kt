package com.neojelll.diaxtracker.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Everything Settings needs to describe the automatic backup: on/off, where it goes, how the last one went. */
data class AutoBackupState(
    val enabled: Boolean,
    val folderUri: String?,
    val lastSuccessMillis: Long,
    val lastAttemptFailed: Boolean
)

/**
 * State of the nightly automatic backup. The background worker writes the outcome of each run
 * here, so [observe] exists to let Settings update while it's open.
 */
class BackupPreferencesStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isAutoBackupEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    fun folderUri(): String? = prefs.getString(KEY_FOLDER, null)

    fun snapshot() = AutoBackupState(
        enabled = isAutoBackupEnabled(),
        folderUri = folderUri(),
        lastSuccessMillis = prefs.getLong(KEY_LAST_SUCCESS, 0L),
        lastAttemptFailed = prefs.getBoolean(KEY_LAST_FAILED, false)
    )

    fun enableAutoBackup(folderUri: String) {
        prefs.edit()
            .putBoolean(KEY_ENABLED, true)
            .putString(KEY_FOLDER, folderUri)
            .putBoolean(KEY_LAST_FAILED, false)
            .apply()
    }

    fun disableAutoBackup() {
        prefs.edit()
            .putBoolean(KEY_ENABLED, false)
            .remove(KEY_FOLDER)
            .remove(KEY_LAST_SUCCESS)
            .putBoolean(KEY_LAST_FAILED, false)
            .apply()
    }

    fun recordSuccess(atMillis: Long) {
        prefs.edit().putLong(KEY_LAST_SUCCESS, atMillis).putBoolean(KEY_LAST_FAILED, false).apply()
    }

    fun recordFailure() {
        prefs.edit().putBoolean(KEY_LAST_FAILED, true).apply()
    }

    fun observe(): Flow<AutoBackupState> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(snapshot()) }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(snapshot())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    companion object {
        private const val PREFS_NAME = "backup_preferences"
        private const val KEY_ENABLED = "auto_backup_enabled"
        private const val KEY_FOLDER = "auto_backup_folder"
        private const val KEY_LAST_SUCCESS = "auto_backup_last_success"
        private const val KEY_LAST_FAILED = "auto_backup_last_failed"
    }
}
