package com.neojelll.diaxtracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class DiaryDao {
    @Insert
    abstract suspend fun insert(entry: DiaryEntry): Long

    @Update
    abstract suspend fun update(entry: DiaryEntry)

    @Delete
    abstract suspend fun delete(entry: DiaryEntry)

    @Query("SELECT * FROM diary_entries ORDER BY createdAt DESC")
    abstract fun getAllEntries(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries ORDER BY createdAt DESC LIMIT 1")
    abstract suspend fun getMostRecentEntry(): DiaryEntry?

    @Insert
    abstract suspend fun insertEntryProducts(products: List<DiaryEntryProduct>)

    @Query("DELETE FROM diary_entry_products WHERE diaryEntryId = :entryId")
    abstract suspend fun deleteEntryProducts(entryId: Long)

    @Query("SELECT * FROM diary_entry_products WHERE diaryEntryId = :entryId ORDER BY sortOrder ASC")
    abstract suspend fun getEntryProducts(entryId: Long): List<DiaryEntryProduct>

    @Transaction
    open suspend fun insertWithProducts(entry: DiaryEntry, products: List<DiaryEntryProduct>): Long {
        val entryId = insert(entry)
        if (products.isNotEmpty()) {
            insertEntryProducts(products.map { it.copy(diaryEntryId = entryId) })
        }
        return entryId
    }

    @Transaction
    open suspend fun updateWithProducts(entry: DiaryEntry, products: List<DiaryEntryProduct>) {
        update(entry)
        deleteEntryProducts(entry.id)
        if (products.isNotEmpty()) {
            insertEntryProducts(products.map { it.copy(diaryEntryId = entry.id) })
        }
    }
}
