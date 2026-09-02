package com.neojelll.diaxtracker.sensor

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object PostMealScheduler {
    fun scheduleFollowUps(context: Context, entryId: Long) {
        val workManager = WorkManager.getInstance(context)
        val tag = tagFor(entryId)
        listOf(1L, 2L, 3L, 4L).forEach { hours ->
            workManager.enqueueUniqueWork(
                "$tag:${hours}h",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<PostMealCheckWorker>()
                    .setInitialDelay(hours, TimeUnit.HOURS)
                    .addTag(tag)
                    .build()
            )
        }
    }

    fun cancelFollowUps(context: Context, entryId: Long) {
        WorkManager.getInstance(context).cancelAllWorkByTag(tagFor(entryId))
    }

    private fun tagFor(entryId: Long) = "post_meal_entry_$entryId"
}
