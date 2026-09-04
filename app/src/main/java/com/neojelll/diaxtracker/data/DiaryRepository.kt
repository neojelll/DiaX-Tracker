package com.neojelll.diaxtracker.data

import kotlinx.coroutines.flow.Flow

class DiaryRepository(
    private val dao: DiaryDao,
    private val mealPresetDao: MealPresetDao
) {
    val allEntries: Flow<List<DiaryEntry>> = dao.getAllEntries()
    val allMealPresets: Flow<List<MealPreset>> = mealPresetDao.getAllPresets()

    suspend fun insert(entry: DiaryEntry): Long = dao.insert(entry)
    suspend fun update(entry: DiaryEntry) = dao.update(entry)
    suspend fun delete(entry: DiaryEntry) = dao.delete(entry)

    suspend fun insertMealPreset(preset: MealPreset): Long = mealPresetDao.insert(preset)
    suspend fun updateMealPreset(preset: MealPreset) = mealPresetDao.update(preset)
    suspend fun deleteMealPreset(preset: MealPreset) = mealPresetDao.delete(preset)
}
