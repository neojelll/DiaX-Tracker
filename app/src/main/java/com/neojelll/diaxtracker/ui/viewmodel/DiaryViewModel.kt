package com.neojelll.diaxtracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.neojelll.diaxtracker.data.DiaryDatabase
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.DiaryRepository
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
        DiaryDatabase.getDatabase(application).diaryDao()
    )
    private val sensorReadingStore = SensorReadingStore(application)

    val entries: StateFlow<List<DiaryEntry>> = repository.allEntries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _sensorAvailable = MutableStateFlow(sensorReadingStore.getLatestReading() != null)
    val sensorAvailable: StateFlow<Boolean> = _sensorAvailable.asStateFlow()

    private val insulinTicker = flow {
        while (true) {
            emit(Unit)
            delay(INSULIN_CHECK_INTERVAL_MILLIS)
        }
    }

    val activeInsulinEntry: StateFlow<DiaryEntry?> = combine(entries, insulinTicker) { list, _ ->
        list
            .filter { it.shortInsulinDose != null }
            .maxByOrNull { it.createdAt }
            ?.takeIf { Duration.between(it.createdAt, LocalDateTime.now()) < Duration.ofHours(4) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
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
        shortInsulinDose: Float?,
        longInsulinDose: Float?,
        notes: String,
        photoPath: String?,
        createdAt: LocalDateTime
    ) {
        viewModelScope.launch {
            val sensorReading = sensorReadingStore.getLatestReading()
            val entryId = repository.insert(
                DiaryEntry(
                    bloodSugar = bloodSugar ?: sensorReading,
                    sugarSource = when {
                        bloodSugar != null -> SugarSource.MANUAL
                        sensorReading != null -> SugarSource.SENSOR
                        else -> null
                    },
                    breadUnits = breadUnits,
                    shortInsulinDose = shortInsulinDose,
                    longInsulinDose = longInsulinDose,
                    notes = notes,
                    photoPath = photoPath,
                    createdAt = createdAt
                )
            )
            if (shortInsulinDose != null || longInsulinDose != null) {
                PostMealScheduler.scheduleFollowUps(getApplication(), entryId)
            }
        }
    }

    fun updateEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            repository.update(entry)
        }
    }

    fun deleteEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            PostMealScheduler.cancelFollowUps(getApplication(), entry.id)
            repository.delete(entry)
            PhotoStore.deletePhoto(entry.photoPath)
        }
    }

    private companion object {
        const val SENSOR_POLL_INTERVAL_MILLIS = 30_000L
        const val INSULIN_CHECK_INTERVAL_MILLIS = 30_000L
    }
}
