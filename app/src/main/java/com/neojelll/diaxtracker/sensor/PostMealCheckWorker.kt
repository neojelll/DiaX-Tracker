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
        if (dao.countEntriesBetween(target.minus(MIN_GAP_BETWEEN_AUTO_ENTRIES), target.plus(MIN_GAP_BETWEEN_AUTO_ENTRIES)) > 0) {
            return Result.success()
        }

        dao.insert(
            DiaryEntry(
                bloodSugar = reading.bloodSugar,
                sugarSource = SugarSource.SENSOR,
                shortInsulinDose = null,
                longInsulinDose = null,
                notes = "",
                createdAt = target
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
        val READING_TOLERANCE: Duration = Duration.ofMinutes(5)
    }
}
