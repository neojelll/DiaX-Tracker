package com.neojelll.diaxtracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.neojelll.diaxtracker.data.DiaryDatabase
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.DiaryRepository
import com.neojelll.diaxtracker.sensor.PostMealScheduler
import com.neojelll.diaxtracker.sensor.SensorReadingStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    fun addEntry(
        bloodSugar: Float?,
        shortInsulinDose: Float?,
        longInsulinDose: Float?,
        notes: String,
        createdAt: LocalDateTime
    ) {
        viewModelScope.launch {
            repository.insert(
                DiaryEntry(
                    bloodSugar = bloodSugar ?: sensorReadingStore.getLatestReading(),
                    shortInsulinDose = shortInsulinDose,
                    longInsulinDose = longInsulinDose,
                    notes = notes,
                    createdAt = createdAt
                )
            )
            PostMealScheduler.scheduleFollowUps(getApplication())
        }
    }
}
