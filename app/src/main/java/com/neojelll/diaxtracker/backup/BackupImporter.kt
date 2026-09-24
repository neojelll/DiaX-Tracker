package com.neojelll.diaxtracker.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.withTransaction
import com.neojelll.diaxtracker.data.DiaryDatabase
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.DiaryEntryProduct
import com.neojelll.diaxtracker.data.MealPresetWithProducts
import java.io.File
import java.util.UUID
import java.util.zip.ZipInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Adds the contents of a backup archive to the current data - nothing that is already there is
 * touched or removed. The archive's database is opened as a Room database of its own (created from
 * the extracted file), so backups made by older app versions are brought up to the current schema by
 * the regular migrations instead of needing a reader per format.
 */
object BackupImporter {
    sealed interface ImportResult {
        data class Success(val summary: Summary) : ImportResult
        data object InvalidFile : ImportResult
        data object Failure : ImportResult
    }

    /** What a merge did: entries added / skipped as duplicates, presets added, sensor readings added. */
    data class Summary(val entriesAdded: Int, val duplicatesSkipped: Int, val presetsAdded: Int, val readingsAdded: Int)

    private const val TAG = "BackupImporter"
    private const val DB_NAME = "diary_database"
    private const val PHOTOS_DIR_NAME = "entry_photos"
    private const val WORK_DIR_NAME = "backup_import"
    private const val TEMP_DB_NAME = "backup_import_tmp"
    private val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)

    suspend fun import(context: Context, source: Uri): ImportResult = withContext(Dispatchers.IO) {
        val workDir = File(context.cacheDir, WORK_DIR_NAME).apply { deleteRecursively(); mkdirs() }
        context.deleteDatabase(TEMP_DB_NAME)
        val copiedPhotos = mutableListOf<File>()
        try {
            val extractedDb = extract(context, source, workDir) ?: return@withContext ImportResult.InvalidFile
            val backup = try {
                DiaryDatabase.newBuilder(context, TEMP_DB_NAME).createFromFile(extractedDb).build()
            } catch (e: Exception) {
                Log.w(TAG, "Archive database can't be opened", e)
                return@withContext ImportResult.InvalidFile
            }

            val backupEntries: List<DiaryEntry>
            val backupProducts: Map<Long, List<DiaryEntryProduct>>
            val backupPresets: List<MealPresetWithProducts>
            try {
                backupEntries = backup.diaryDao().getAllEntries().first()
                backupProducts = backup.diaryDao().getAllEntryProducts().first().groupBy { it.diaryEntryId }
                backupPresets = backup.mealPresetDao().getAllPresetsWithProducts().first()
            } catch (e: Exception) {
                Log.w(TAG, "Archive database can't be read", e)
                return@withContext ImportResult.InvalidFile
            } finally {
                backup.close()
            }

            val live = DiaryDatabase.getDatabase(context)
            val photosDir = File(context.filesDir, PHOTOS_DIR_NAME).apply { mkdirs() }
            var added = 0
            var skipped = 0
            var presetsAdded = 0

            live.withTransaction {
                val diaryDao = live.diaryDao()
                for (entry in backupEntries.sortedBy { it.createdAt }) {
                    if (isDuplicateEntry(diaryDao.getEntriesAt(entry.createdAt), entry)) {
                        skipped++
                        continue
                    }
                    val photoPath = adoptPhoto(entry.photoPath, File(workDir, PHOTOS_DIR_NAME), photosDir)
                        ?.also { copiedPhotos += File(it) }
                    diaryDao.insertWithProducts(
                        entry.copy(id = 0, photoPath = photoPath),
                        backupProducts[entry.id].orEmpty().map { it.copy(id = 0, diaryEntryId = 0) }
                    )
                    added++
                }

                val presetDao = live.mealPresetDao()
                val livePresetNames = presetDao.getAllPresetsWithProducts().first().map { it.preset.name }.toSet()
                for (preset in backupPresets.filter { it.preset.name !in livePresetNames }) {
                    presetDao.upsertPresetWithProducts(
                        preset.preset.copy(id = 0),
                        preset.products.map { it.copy(id = 0, mealPresetId = 0) }
                    )
                    presetsAdded++
                }
            }

            val readingsAdded = mergeSensorReadings(live, context.getDatabasePath(TEMP_DB_NAME))
            ImportResult.Success(Summary(added, skipped, presetsAdded, readingsAdded))
        } catch (e: Exception) {
            Log.e(TAG, "Backup import failed", e)
            // Entries roll back with the transaction; the photo files copied for them would be orphans.
            copiedPhotos.forEach { it.delete() }
            ImportResult.Failure
        } finally {
            context.deleteDatabase(TEMP_DB_NAME)
            workDir.deleteRecursively()
        }
    }

    /** Unpacks the archive into [workDir]; returns the database file, or null if it isn't a backup. */
    private fun extract(context: Context, source: Uri, workDir: File): File? {
        val dbFile = File(workDir, DB_NAME)
        val photosDir = File(workDir, PHOTOS_DIR_NAME).apply { mkdirs() }
        val stream = context.contentResolver.openInputStream(source) ?: return null
        stream.use { input ->
            ZipInputStream(input).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    when {
                        entry.name == DB_NAME -> dbFile.outputStream().use { zip.copyTo(it) }
                        // Only the file name is used, so a crafted entry can't write outside the folder.
                        entry.name.startsWith("$PHOTOS_DIR_NAME/") && !entry.isDirectory ->
                            File(photosDir, File(entry.name).name).outputStream().use { zip.copyTo(it) }
                    }
                }
            }
        }
        if (!dbFile.exists() || dbFile.length() < SQLITE_HEADER.size) return null
        val header = ByteArray(SQLITE_HEADER.size).also { buffer -> dbFile.inputStream().use { it.read(buffer) } }
        return dbFile.takeIf { header.contentEquals(SQLITE_HEADER) }
    }

    /** Copies the backed-up photo under a fresh name (so it never shares a file with an existing entry). */
    private fun adoptPhoto(originalPath: String?, archivePhotos: File, photosDir: File): String? {
        val name = originalPath?.let { File(it).name } ?: return null
        val source = File(archivePhotos, name).takeIf { it.exists() } ?: return null
        val target = File(photosDir, "${UUID.randomUUID()}.jpg")
        source.copyTo(target)
        return target.absolutePath
    }

    /**
     * Set-based on purpose: the reading log can hold hundreds of thousands of rows, so it is merged
     * inside SQLite (attach, insert what's missing, detach) instead of through objects in memory.
     */
    private fun mergeSensorReadings(live: DiaryDatabase, backupDb: File): Int {
        val sqlite = live.openHelper.writableDatabase
        sqlite.execSQL("ATTACH DATABASE ? AS backup", arrayOf<Any>(backupDb.path))
        try {
            var inserted = 0
            sqlite.beginTransaction()
            try {
                sqlite.execSQL(
                    "INSERT INTO sensor_readings_log (timestamp, bloodSugar) " +
                        "SELECT b.timestamp, b.bloodSugar FROM backup.sensor_readings_log b " +
                        "WHERE NOT EXISTS (SELECT 1 FROM main.sensor_readings_log m WHERE m.timestamp = b.timestamp)"
                )
                sqlite.query("SELECT changes()").use { if (it.moveToFirst()) inserted = it.getInt(0) }
                sqlite.setTransactionSuccessful()
            } finally {
                sqlite.endTransaction()
            }
            return inserted
        } finally {
            sqlite.execSQL("DETACH DATABASE backup")
        }
    }
}

/**
 * The same record if it happened at the same moment with the same values. Different values at the
 * same moment are a genuine conflict and both are kept, so importing never loses data. The photo
 * path is ignored: it points into a different app data folder in the archive.
 */
internal fun isDuplicateEntry(existing: List<DiaryEntry>, candidate: DiaryEntry): Boolean {
    val normalized = candidate.copy(id = 0, photoPath = null)
    return existing.any { it.copy(id = 0, photoPath = null) == normalized }
}
