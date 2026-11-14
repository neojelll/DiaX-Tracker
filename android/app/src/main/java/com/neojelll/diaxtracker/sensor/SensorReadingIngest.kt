package com.neojelll.diaxtracker.sensor

import android.content.Context
import android.util.Log
import com.neojelll.diaxtracker.data.DiaryDatabase
import com.neojelll.diaxtracker.data.SensorReadingLog
import com.neojelll.diaxtracker.time.AppClock
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "SensorReadingIngest"

/** Shared by both the always-on dynamic receiver and the manifest-declared one below. */
fun persistSensorReading(context: Context, bloodSugar: Float, timestamp: Long) {
    SensorReadingStore(context).save(bloodSugar, timestamp)
    CoroutineScope(Dispatchers.IO).launch {
        try {
            DiaryDatabase.getDatabase(context).sensorReadingLogDao().insert(
                SensorReadingLog(
                    timestamp = Instant.ofEpochMilli(timestamp)
                        .atZone(AppClock.zone)
                        .toLocalDateTime(),
                    bloodSugar = bloodSugar
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist sensor reading", e)
        }
    }
}
