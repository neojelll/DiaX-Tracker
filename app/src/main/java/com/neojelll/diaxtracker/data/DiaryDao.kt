package com.neojelll.diaxtracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

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

    @Query("SELECT * FROM diary_entries WHERE createdAt = :createdAt")
    abstract suspend fun getEntriesAt(createdAt: LocalDateTime): List<DiaryEntry>

    @Query("SELECT COUNT(*) FROM diary_entries WHERE createdAt BETWEEN :from AND :to")
    abstract suspend fun countEntriesBetween(from: LocalDateTime, to: LocalDateTime): Int

    @Query("DELETE FROM diary_entries")
    abstract suspend fun deleteAllEntries()

    @Insert
    abstract suspend fun insertEntryProducts(products: List<DiaryEntryProduct>)

    @Query("DELETE FROM diary_entry_products WHERE diaryEntryId = :entryId")
    abstract suspend fun deleteEntryProducts(entryId: Long)

    @Query("SELECT * FROM diary_entry_products WHERE diaryEntryId = :entryId ORDER BY sortOrder ASC")
    abstract suspend fun getEntryProducts(entryId: Long): List<DiaryEntryProduct>

    @Query("SELECT * FROM diary_entry_products")
    abstract fun getAllEntryProducts(): Flow<List<DiaryEntryProduct>>

    @Transaction
    open suspend fun insertWithProducts(entry: DiaryEntry, products: List<DiaryEntryProduct>): Long {
        val entryId = insert(entry)
        if (products.isNotEmpty()) {
            insertEntryProducts(products.map { it.copy(diaryEntryId = entryId) })
        }
        return entryId
    }

    @Insert
    abstract suspend fun insertEntries(entries: List<DiaryEntry>)

    /** Puts deleted entries back under their original ids, along with their products. */
    @Transaction
    open suspend fun restore(entries: List<DiaryEntry>, products: List<DiaryEntryProduct>) {
        insertEntries(entries)
        if (products.isNotEmpty()) insertEntryProducts(products)
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
