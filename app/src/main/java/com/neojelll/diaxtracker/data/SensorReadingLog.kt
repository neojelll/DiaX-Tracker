package com.neojelll.diaxtracker.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "sensor_readings_log", indices = [Index("timestamp")])
data class SensorReadingLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: LocalDateTime,
    val bloodSugar: Float
)
