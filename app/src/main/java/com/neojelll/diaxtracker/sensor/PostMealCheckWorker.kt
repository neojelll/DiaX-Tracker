package com.neojelll.diaxtracker.sensor

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.neojelll.diaxtracker.data.DiaryDatabase
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.SugarSource
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class PostMealCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val target = targetTime()
        val db = DiaryDatabase.getDatabase(applicationContext)

        // The job can run minutes (or more) after the target, so read the value at the target time
        // from the reading log rather than taking whatever the latest reading happens to be.
        val reading = nearestSensorReading(
            db.sensorReadingLogDao().getReadingsBetween(target.minus(READING_TOLERANCE), target.plus(READING_TOLERANCE)),
            target
        ) ?: return Result.success()

        val dao = db.diaryDao()
        val sourceId = inputData.getLong(PostMealScheduler.KEY_SOURCE_ENTRY_ID, 0L).takeIf { it > 0L }
        val hour = inputData.getInt(PostMealScheduler.KEY_HOUR, 0).takeIf { it in 1..4 }

        if (sourceId != null && hour != null) {
            // Each meal owns its four checks: whether another meal has a check nearby is irrelevant,
            // only this meal's own hour matters. A meal deleted since is nothing to check for.
            if (dao.getEntryById(sourceId) == null || dao.countAutoChecks(sourceId, hour) > 0) return Result.success()
        } else if (dao.countEntriesBetween(target.minus(MIN_GAP_BETWEEN_AUTO_ENTRIES), target.plus(MIN_GAP_BETWEEN_AUTO_ENTRIES)) > 0) {
            // Work enqueued by an older version knows no meal; it keeps the old "nothing else nearby" rule.
            return Result.success()
        }

        dao.insert(
            DiaryEntry(
                bloodSugar = reading.bloodSugar,
                sugarSource = SugarSource.SENSOR,
                shortInsulinDose = null,
                longInsulinDose = null,
                notes = "",
                createdAt = target,
                sourceEntryId = sourceId.takeIf { hour != null },
                sourceHour = hour.takeIf { sourceId != null }
            )
        )
        return Result.success()
    }

    // Work enqueued by an older version carries no target; treat it as "now", as it used to be.
    private fun targetTime(): LocalDateTime =
        inputData.getLong(PostMealScheduler.KEY_TARGET_MILLIS, 0L)
            .takeIf { it > 0L }
            ?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime() }
            ?: LocalDateTime.now()

    private companion object {
        val MIN_GAP_BETWEEN_AUTO_ENTRIES: Duration = Duration.ofMinutes(20)
        // Same reach as the manual-entry sugar auto-fill: a reading up to 10 min off the target is
        // still worth having (the entry is stamped at the exact target either way), a missing check isn't.
        val READING_TOLERANCE: Duration = Duration.ofMinutes(10)
    }
}
