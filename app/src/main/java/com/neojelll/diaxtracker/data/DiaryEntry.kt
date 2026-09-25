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
    val mealLabel: String? = null,
    val shortInsulinDose: Float?,
    val longInsulinDose: Float?,
    val notes: String,
    val photoPath: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/** Food was logged: a preset or a manual amount. Sugar checks and insulin-only entries aren't meals. */
fun DiaryEntry.isMeal(): Boolean = mealLabel != null || breadUnits != null
