package com.neojelll.diaxtracker.data

import kotlinx.coroutines.flow.Flow

class DiaryRepository(
    private val dao: DiaryDao,
    private val mealPresetDao: MealPresetDao
) {
    val allEntries: Flow<List<DiaryEntry>> = dao.getAllEntries()
    val allMealPresets: Flow<List<MealPresetWithProducts>> = mealPresetDao.getAllPresetsWithProducts()

    suspend fun insert(entry: DiaryEntry): Long = dao.insert(entry)
    suspend fun update(entry: DiaryEntry) = dao.update(entry)
    suspend fun delete(entry: DiaryEntry) = dao.delete(entry)

    suspend fun insertWithProducts(entry: DiaryEntry, products: List<DiaryEntryProduct>): Long =
        dao.insertWithProducts(entry, products)
    suspend fun updateWithProducts(entry: DiaryEntry, products: List<DiaryEntryProduct>) =
        dao.updateWithProducts(entry, products)
    suspend fun getEntryProducts(entryId: Long): List<DiaryEntryProduct> = dao.getEntryProducts(entryId)

    suspend fun saveMealPreset(preset: MealPreset, products: List<MealPresetProduct>): Long =
        mealPresetDao.upsertPresetWithProducts(preset, products)
    suspend fun deleteMealPreset(preset: MealPreset) = mealPresetDao.delete(preset)
}
