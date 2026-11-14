package com.neojelll.diaxtracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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

    @Query("SELECT * FROM diary_entries WHERE id = :id")
    abstract suspend fun getEntryById(id: Long): DiaryEntry?

    @Query("SELECT COUNT(*) FROM diary_entries WHERE sourceEntryId = :sourceEntryId AND sourceHour = :hour")
    abstract suspend fun countAutoChecks(sourceEntryId: Long, hour: Int): Int

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceEntries(entries: List<DiaryEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceEntryProducts(products: List<DiaryEntryProduct>)

    /**
     * Puts deleted entries back under their original ids, along with their products. Replaces on
     * conflict so a product row that survived the delete doesn't fail the whole restore.
     */
    @Transaction
    open suspend fun restore(entries: List<DiaryEntry>, products: List<DiaryEntryProduct>) {
        replaceEntries(entries)
        if (products.isNotEmpty()) replaceEntryProducts(products)
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
