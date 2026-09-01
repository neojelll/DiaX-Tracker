package com.neojelll.diaxtracker.sensor

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object PostMealScheduler {
    fun scheduleFollowUps(context: Context) {
        val workManager = WorkManager.getInstance(context)
        listOf(1L, 2L, 3L, 4L).forEach { hours ->
            workManager.enqueue(
                OneTimeWorkRequestBuilder<PostMealCheckWorker>()
                    .setInitialDelay(hours, TimeUnit.HOURS)
                    .build()
            )
        }
    }
}
