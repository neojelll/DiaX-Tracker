package com.neojelll.diaxtracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "diary_entry_products",
    foreignKeys = [
        ForeignKey(
            entity = DiaryEntry::class,
            parentColumns = ["id"],
            childColumns = ["diaryEntryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("diaryEntryId")]
)
data class DiaryEntryProduct(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val diaryEntryId: Long,
    val name: String,
    val breadUnits: Float,
    val sortOrder: Int
)
