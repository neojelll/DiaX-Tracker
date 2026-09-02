package com.neojelll.diaxtracker.sensor

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.neojelll.diaxtracker.data.DiaryDatabase
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.SugarSource
import java.time.LocalDateTime

class PostMealCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val sugar = SensorReadingStore(applicationContext).getLatestReading() ?: return Result.success()

        DiaryDatabase.getDatabase(applicationContext).diaryDao().insert(
            DiaryEntry(
                bloodSugar = sugar,
                sugarSource = SugarSource.SENSOR,
                shortInsulinDose = null,
                longInsulinDose = null,
                notes = "",
                createdAt = LocalDateTime.now()
            )
        )
        return Result.success()
    }
}
