package com.neojelll.diaxtracker.data

import kotlinx.coroutines.flow.Flow

class DiaryRepository(private val dao: DiaryDao) {
    val allEntries: Flow<List<DiaryEntry>> = dao.getAllEntries()

    suspend fun insert(entry: DiaryEntry) = dao.insert(entry)
}
