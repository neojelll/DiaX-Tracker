package com.neojelll.diaxtracker.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.neojelll.diaxtracker.data.BackupPreferencesStore
import java.io.IOException
import java.time.LocalDateTime

/** One automatic backup: a full export into the chosen folder, then trimming old automatic ones. */
class AutoBackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val store = BackupPreferencesStore(applicationContext)
        if (!store.isAutoBackupEnabled()) return Result.success()
        val folder = store.folderUri()?.let(Uri::parse) ?: return Result.success()

        return try {
            val file = BackupFolder.createFile(applicationContext, folder, autoBackupFileName(LocalDateTime.now()))
                ?: throw IOException("The backup folder refused to create a file")
            if (!BackupExporter.export(applicationContext, file, from = null)) {
                BackupFolder.delete(applicationContext, file)
                throw IOException("Export failed")
            }
            // Trimming is housekeeping: a hiccup there must not make a good backup count as failed.
            runCatching { BackupFolder.deleteStale(applicationContext, folder) }
                .onFailure { Log.w(TAG, "Couldn't trim old automatic backups", it) }
            store.recordSuccess(System.currentTimeMillis())
            Result.success()
        } catch (e: Exception) {
            // Also covers the folder's access being revoked (SecurityException); Settings shows it.
            Log.e(TAG, "Automatic backup failed", e)
            store.recordFailure()
            Result.failure()
        }
    }

    private companion object {
        const val TAG = "AutoBackupWorker"
    }
}
