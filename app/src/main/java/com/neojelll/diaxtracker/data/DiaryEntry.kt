package com.neojelll.diaxtracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bloodSugar: Float?,
    val insulinDose: Float?,
    val mealDescription: String,
    val notes: String,
    val mealTime: MealTime,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class MealTime(val label: String) {
    BEFORE_MEAL("До еды"),
    AFTER_MEAL("После еды"),
    FASTING("Натощак"),
    BEDTIME("Перед сном")
}
