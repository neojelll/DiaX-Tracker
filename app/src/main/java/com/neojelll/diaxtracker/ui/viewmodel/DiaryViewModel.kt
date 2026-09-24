package com.neojelll.diaxtracker.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.backup.AutoBackupScheduler
import com.neojelll.diaxtracker.data.AutoBackupState
import com.neojelll.diaxtracker.data.BackupPreferencesStore
import com.neojelll.diaxtracker.data.DiaryDatabase
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.DiaryEntryProduct
import com.neojelll.diaxtracker.data.DiaryRepository
import com.neojelll.diaxtracker.data.DisclaimerStore
import com.neojelll.diaxtracker.data.GlucoseRange
import com.neojelll.diaxtracker.data.GlucoseRangeStore
import com.neojelll.diaxtracker.data.InsulinSettingsStore
import com.neojelll.diaxtracker.data.MealPreset
import com.neojelll.diaxtracker.data.MealPresetProduct
import com.neojelll.diaxtracker.data.MealPresetWithProducts
import com.neojelll.diaxtracker.data.SugarSource
import com.neojelll.diaxtracker.photo.PhotoStore
import com.neojelll.diaxtracker.sensor.PostMealScheduler
import com.neojelll.diaxtracker.sensor.SensorReadingStore
import com.neojelll.diaxtracker.sensor.nearestSensorReading
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val insulinSettingsStore = InsulinSettingsStore(application)
    private val backupPreferencesStore = BackupPreferencesStore(application)
    private val disclaimerStore = DisclaimerStore(application)

    val entries: StateFlow<List<DiaryEntry>> = repository.allEntries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    /** Every entry's product/ingredient rows, for History's search - see per-entry [getEntryProducts] for display. */
    val entryProducts: StateFlow<List<DiaryEntryProduct>> = repository.allEntryProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val mealPresets: StateFlow<List<MealPresetWithProducts>> = repository.allMealPresets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _sensorWarningVisible = MutableStateFlow(computeSensorWarningVisible())
    val sensorWarningVisible: StateFlow<Boolean> = _sensorWarningVisible.asStateFlow()

    private val _glucoseRange = MutableStateFlow(glucoseRangeStore.getRange())
    val glucoseRange: StateFlow<GlucoseRange> = _glucoseRange.asStateFlow()

    private val _insulinDurationHours = MutableStateFlow(insulinSettingsStore.getDurationHours())
    val insulinDurationHours: StateFlow<Float> = _insulinDurationHours.asStateFlow()

    val autoBackup: StateFlow<AutoBackupState> = backupPreferencesStore.observe().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = backupPreferencesStore.snapshot()
    )

    private val _disclaimerAccepted = MutableStateFlow(disclaimerStore.isAccepted())
    val disclaimerAccepted: StateFlow<Boolean> = _disclaimerAccepted.asStateFlow()

    private val _errorEvents = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val errorEvents: SharedFlow<Int> = _errorEvents.asSharedFlow()

    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Operation failed", e)
                _errorEvents.tryEmit(R.string.error_generic)
            }
        }
    }

    private val insulinTicker = flow {
        while (true) {
            emit(Unit)
            delay(INSULIN_CHECK_INTERVAL_MILLIS)
        }
    }

    val activeInsulinEntries: StateFlow<List<DiaryEntry>> = combine(entries, insulinTicker, _insulinDurationHours) { list, _, durationHours ->
        val now = LocalDateTime.now()
        val duration = Duration.ofMinutes((durationHours * 60).toLong())
        list
            .filter { it.shortInsulinDose != null && Duration.between(it.createdAt, now) < duration }
            .sortedByDescending { it.createdAt }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            while (true) {
                _sensorWarningVisible.value = computeSensorWarningVisible()
                delay(SENSOR_POLL_INTERVAL_MILLIS)
            }
        }
    }

    // Only worth flagging for someone who's been getting readings recently - otherwise this is
    // either a fresh install with no sensor at all, or a sensor pairing abandoned a while ago,
    // and neither should nag the person about "stale" data that was never really flowing.
    private fun computeSensorWarningVisible(): Boolean =
        sensorReadingStore.getLatestReading() == null &&
            sensorReadingStore.hasReadingWithin(SensorReadingStore.RECENT_ACTIVITY_WINDOW_MILLIS)

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
        launchSafely {
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
                PostMealScheduler.scheduleFollowUps(getApplication(), entryId, createdAt)
            }
        }
    }

    suspend fun sensorReadingNear(referenceTime: LocalDateTime): Float? {
        val windowStart = referenceTime.minusMinutes(SENSOR_FALLBACK_TOLERANCE_MINUTES)
        val windowEnd = referenceTime.plusMinutes(SENSOR_FALLBACK_TOLERANCE_MINUTES)
        return nearestSensorReading(repository.getSensorReadingsBetween(windowStart, windowEnd), referenceTime)
            ?.bloodSugar
    }

    fun updateEntry(entry: DiaryEntry, mealProducts: List<DiaryEntryProduct>) {
        launchSafely {
            repository.updateWithProducts(entry, mealProducts)
            if (entry.shortInsulinDose != null || entry.longInsulinDose != null) {
                PostMealScheduler.scheduleFollowUps(getApplication(), entry.id, entry.createdAt)
            } else {
                PostMealScheduler.cancelFollowUps(getApplication(), entry.id)
            }
        }
    }

    suspend fun getEntryProducts(entryId: Long): List<DiaryEntryProduct> = repository.getEntryProducts(entryId)

    fun deleteEntry(entry: DiaryEntry) {
        launchSafely {
            PostMealScheduler.cancelFollowUps(getApplication(), entry.id)
            repository.delete(entry)
            PhotoStore.deletePhoto(entry.photoPath)
        }
    }

    fun saveMealPreset(id: Long, name: String, comment: String, products: List<MealPresetProduct>) {
        launchSafely {
            repository.saveMealPreset(MealPreset(id = id, name = name, comment = comment), products)
        }
    }

    fun deleteMealPreset(preset: MealPreset) {
        launchSafely {
            repository.deleteMealPreset(preset)
        }
    }

    fun setGlucoseRange(low: Float, high: Float) {
        val range = GlucoseRange(low, high)
        glucoseRangeStore.saveRange(range)
        _glucoseRange.value = range
    }

    fun setInsulinDurationHours(hours: Float) {
        insulinSettingsStore.saveDurationHours(hours)
        _insulinDurationHours.value = hours
    }

    fun enableAutoBackup(folder: Uri) {
        val app = getApplication<Application>()
        try {
            app.contentResolver.takePersistableUriPermission(folder, FOLDER_ACCESS_FLAGS)
        } catch (e: SecurityException) {
            Log.e(TAG, "Couldn't keep access to the backup folder", e)
            _errorEvents.tryEmit(R.string.error_generic)
            return
        }
        backupPreferencesStore.enableAutoBackup(folder.toString())
        AutoBackupScheduler.enable(app)
    }

    fun disableAutoBackup() {
        val app = getApplication<Application>()
        backupPreferencesStore.folderUri()?.let { stored ->
            runCatching { app.contentResolver.releasePersistableUriPermission(Uri.parse(stored), FOLDER_ACCESS_FLAGS) }
        }
        backupPreferencesStore.disableAutoBackup()
        AutoBackupScheduler.disable(app)
    }

    fun acceptDisclaimer() {
        disclaimerStore.setAccepted()
        _disclaimerAccepted.value = true
    }

    fun deleteAllEntries() {
        launchSafely {
            val snapshot = entries.value
            snapshot.forEach { entry ->
                PostMealScheduler.cancelFollowUps(getApplication(), entry.id)
                PhotoStore.deletePhoto(entry.photoPath)
            }
            repository.deleteAllEntries()
        }
    }

    private companion object {
        const val TAG = "DiaryViewModel"
        const val FOLDER_ACCESS_FLAGS = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        const val SENSOR_POLL_INTERVAL_MILLIS = 30_000L
        const val INSULIN_CHECK_INTERVAL_MILLIS = 60_000L
        const val SENSOR_FALLBACK_TOLERANCE_MINUTES = 10L
    }
}
