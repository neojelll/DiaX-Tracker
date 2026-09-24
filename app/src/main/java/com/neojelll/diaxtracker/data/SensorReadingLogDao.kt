package com.neojelll.diaxtracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.time.LocalDateTime

@Dao
interface SensorReadingLogDao {
    @Insert
    suspend fun insert(reading: SensorReadingLog)

    @Insert
    suspend fun insertAll(readings: List<SensorReadingLog>)

    @Query("SELECT * FROM sensor_readings_log WHERE id > :afterId ORDER BY id LIMIT :limit")
    suspend fun getPage(afterId: Long, limit: Int): List<SensorReadingLog>

    @Query("SELECT * FROM sensor_readings_log WHERE timestamp BETWEEN :from AND :to")
    suspend fun getReadingsBetween(from: LocalDateTime, to: LocalDateTime): List<SensorReadingLog>
}
