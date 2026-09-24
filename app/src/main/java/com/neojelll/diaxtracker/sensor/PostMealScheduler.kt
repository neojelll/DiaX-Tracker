package com.neojelll.diaxtracker.sensor

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

private val FOLLOW_UP_HOURS = listOf(1L, 2L, 3L, 4L)

internal data class FollowUp(val hours: Long, val target: LocalDateTime, val delay: Duration)

/** The checks still to come for an entry: those whose [entryTime] + N hours hasn't passed yet. */
internal fun pendingFollowUps(entryTime: LocalDateTime, now: LocalDateTime): List<FollowUp> =
    FOLLOW_UP_HOURS
        .map { hours ->
            val target = entryTime.plusHours(hours)
            FollowUp(hours, target, Duration.between(now, target))
        }
        .filter { it.delay > Duration.ZERO }

object PostMealScheduler {
    const val KEY_TARGET_MILLIS = "target_millis"

    /**
     * Schedules a sugar check at +1h/+2h/+3h/+4h after [entryTime]. WorkManager's initial delay is
     * only a minimum - Doze and OEM battery management routinely run the job minutes late - so each
     * request carries its exact target time, and PostMealCheckWorker stamps the auto entry with it
     * (taking the value from the reading log) instead of with whenever it happened to run.
     */
    fun scheduleFollowUps(context: Context, entryId: Long, entryTime: LocalDateTime) {
        // Targets that already passed are skipped, so REPLACE alone could leave stale work behind
        // when an edit moves the entry - clear the whole set first.
        cancelFollowUps(context, entryId)
        val workManager = WorkManager.getInstance(context)
        val tag = tagFor(entryId)
        pendingFollowUps(entryTime, LocalDateTime.now()).forEach { followUp ->
            val targetMillis = followUp.target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            workManager.enqueueUniqueWork(
                "$tag:${followUp.hours}h",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<PostMealCheckWorker>()
                    .setInitialDelay(followUp.delay.toMillis(), TimeUnit.MILLISECONDS)
                    .setInputData(workDataOf(KEY_TARGET_MILLIS to targetMillis))
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
