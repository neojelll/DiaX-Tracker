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
    val insulinType: InsulinType?,
    val notes: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class InsulinType(val label: String) {
    SHORT("Короткий"),
    LONG("Длинный")
}
