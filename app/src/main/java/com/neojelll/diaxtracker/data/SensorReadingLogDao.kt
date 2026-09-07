package com.neojelll.diaxtracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.time.LocalDateTime

@Dao
interface SensorReadingLogDao {
    @Insert
    suspend fun insert(reading: SensorReadingLog)

    @Query("SELECT * FROM sensor_readings_log WHERE timestamp BETWEEN :from AND :to")
    suspend fun getReadingsBetween(from: LocalDateTime, to: LocalDateTime): List<SensorReadingLog>
}
