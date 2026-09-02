package com.neojelll.diaxtracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

enum class SugarSource { MANUAL, SENSOR }

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bloodSugar: Float?,
    val sugarSource: SugarSource? = null,
    val breadUnits: Float? = null,
    val shortInsulinDose: Float?,
    val longInsulinDose: Float?,
    val notes: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
