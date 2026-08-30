package com.neojelll.later.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.neojelll.later.data.DiaryDatabase
import com.neojelll.later.data.DiaryEntry
import com.neojelll.later.data.DiaryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DiaryRepository(
        DiaryDatabase.getDatabase(application).diaryDao()
    )

    val entries: StateFlow<List<DiaryEntry>> = repository.allEntries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun addEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            repository.insert(entry)
        }
    }
}
