package com.neojelll.diaxtracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.neojelll.diaxtracker.data.DiaryDatabase
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.DiaryEntryProduct
import com.neojelll.diaxtracker.data.DiaryRepository
import com.neojelll.diaxtracker.data.GlucoseRange
import com.neojelll.diaxtracker.data.GlucoseRangeStore
import com.neojelll.diaxtracker.data.MealPreset
import com.neojelll.diaxtracker.data.MealPresetProduct
import com.neojelll.diaxtracker.data.MealPresetWithProducts
import com.neojelll.diaxtracker.data.SugarSource
import com.neojelll.diaxtracker.photo.PhotoStore
import com.neojelll.diaxtracker.sensor.PostMealScheduler
import com.neojelll.diaxtracker.sensor.SensorReadingStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DiaryRepository(
        DiaryDatabase.getDatabase(application).diaryDao(),
        DiaryDatabase.getDatabase(application).mealPresetDao(),
        DiaryDatabase.getDatabase(application).sensorReadingLogDao()
    )
    private val sensorReadingStore = SensorReadingStore(application)
    private val glucoseRangeStore = GlucoseRangeStore(application)

    val entries: StateFlow<List<DiaryEntry>> = repository.allEntries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val mealPresets: StateFlow<List<MealPresetWithProducts>> = repository.allMealPresets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _sensorAvailable = MutableStateFlow(sensorReadingStore.getLatestReading() != null)
    val sensorAvailable: StateFlow<Boolean> = _sensorAvailable.asStateFlow()

    private val _glucoseRange = MutableStateFlow(glucoseRangeStore.getRange())
    val glucoseRange: StateFlow<GlucoseRange> = _glucoseRange.asStateFlow()

    private val insulinTicker = flow {
        while (true) {
            emit(Unit)
            delay(INSULIN_CHECK_INTERVAL_MILLIS)
        }
    }

    val activeInsulinEntries: StateFlow<List<DiaryEntry>> = combine(entries, insulinTicker) { list, _ ->
        val now = LocalDateTime.now()
        list
            .filter { it.shortInsulinDose != null && Duration.between(it.createdAt, now) < Duration.ofHours(4) }
            .sortedByDescending { it.createdAt }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            while (true) {
                _sensorAvailable.value = sensorReadingStore.getLatestReading() != null
                delay(SENSOR_POLL_INTERVAL_MILLIS)
            }
        }
    }

    fun addEntry(
        bloodSugar: Float?,
        breadUnits: Float?,
        mealLabel: String?,
        mealProducts: List<DiaryEntryProduct>,
        shortInsulinDose: Float?,
        longInsulinDose: Float?,
        notes: String,
        photoPath: String?,
        createdAt: LocalDateTime
    ) {
        viewModelScope.launch {
            val sensorReading = sensorReadingNear(createdAt)
            val entryId = repository.insertWithProducts(
                DiaryEntry(
                    bloodSugar = bloodSugar ?: sensorReading,
                    sugarSource = when {
                        bloodSugar != null -> SugarSource.MANUAL
                        sensorReading != null -> SugarSource.SENSOR
                        else -> null
                    },
                    breadUnits = breadUnits,
                    mealLabel = mealLabel,
                    shortInsulinDose = shortInsulinDose,
                    longInsulinDose = longInsulinDose,
                    notes = notes,
                    photoPath = photoPath,
                    createdAt = createdAt
                ),
                mealProducts
            )
            if (shortInsulinDose != null || longInsulinDose != null) {
                PostMealScheduler.scheduleFollowUps(getApplication(), entryId)
            }
        }
    }

    fun sensorReadingNear(referenceTime: LocalDateTime): Float? {
        val minutesFromNow = kotlin.math.abs(Duration.between(referenceTime, LocalDateTime.now()).toMinutes())
        return if (minutesFromNow <= SENSOR_FALLBACK_TOLERANCE_MINUTES) {
            sensorReadingStore.getLatestReading()
        } else {
            null
        }
    }

    fun updateEntry(entry: DiaryEntry, mealProducts: List<DiaryEntryProduct>) {
        viewModelScope.launch {
            repository.updateWithProducts(entry, mealProducts)
            if (entry.shortInsulinDose != null || entry.longInsulinDose != null) {
                PostMealScheduler.scheduleFollowUps(getApplication(), entry.id)
            } else {
                PostMealScheduler.cancelFollowUps(getApplication(), entry.id)
            }
        }
    }

    suspend fun getEntryProducts(entryId: Long): List<DiaryEntryProduct> = repository.getEntryProducts(entryId)

    fun deleteEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            PostMealScheduler.cancelFollowUps(getApplication(), entry.id)
            repository.delete(entry)
            PhotoStore.deletePhoto(entry.photoPath)
        }
    }

    fun saveMealPreset(id: Long, name: String, comment: String, products: List<MealPresetProduct>) {
        viewModelScope.launch {
            repository.saveMealPreset(MealPreset(id = id, name = name, comment = comment), products)
        }
    }

    fun deleteMealPreset(preset: MealPreset) {
        viewModelScope.launch {
            repository.deleteMealPreset(preset)
        }
    }

    fun setGlucoseRange(low: Float, high: Float) {
        val range = GlucoseRange(low, high)
        glucoseRangeStore.saveRange(range)
        _glucoseRange.value = range
    }

    private companion object {
        const val SENSOR_POLL_INTERVAL_MILLIS = 30_000L
        const val INSULIN_CHECK_INTERVAL_MILLIS = 30_000L
        const val SENSOR_FALLBACK_TOLERANCE_MINUTES = 5L
    }
}
