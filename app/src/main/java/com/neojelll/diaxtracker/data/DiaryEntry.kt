package com.neojelll.diaxtracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bloodSugar: Float?,
    val shortInsulinDose: Float?,
    val longInsulinDose: Float?,
    val notes: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
