package com.neojelll.diaxtracker.data

import kotlinx.coroutines.flow.Flow

class DiaryRepository(private val dao: DiaryDao) {
    val allEntries: Flow<List<DiaryEntry>> = dao.getAllEntries()

    suspend fun insert(entry: DiaryEntry): Long = dao.insert(entry)
    suspend fun update(entry: DiaryEntry) = dao.update(entry)
    suspend fun delete(entry: DiaryEntry) = dao.delete(entry)
}
