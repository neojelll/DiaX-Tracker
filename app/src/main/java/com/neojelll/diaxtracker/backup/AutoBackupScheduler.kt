package com.neojelll.diaxtracker.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object AutoBackupScheduler {
    private const val PERIODIC_NAME = "auto_backup_periodic"
    private const val FIRST_RUN_NAME = "auto_backup_first"

    private val constraints = Constraints.Builder()
        .setRequiresBatteryNotLow(true)
        .setRequiresStorageNotLow(true)
        .build()

    /**
     * Backs up once right away - so the person sees it work (and a bad folder shows up now, not
     * tomorrow night) - and then every evening around [AUTO_BACKUP_HOUR]. The periodic run is
     * WorkManager's own: it survives reboots and app updates, and a run the phone slept through is
     * made up for later instead of skipped.
     */
    fun enable(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniqueWork(
            FIRST_RUN_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<AutoBackupWorker>().setConstraints(constraints).build()
        )
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<AutoBackupWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delayUntilNextEvening(LocalDateTime.now()).toMillis(), TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()
        )
    }

    fun disable(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(PERIODIC_NAME)
        workManager.cancelUniqueWork(FIRST_RUN_NAME)
    }
}
