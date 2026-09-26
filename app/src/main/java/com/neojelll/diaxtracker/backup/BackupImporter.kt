package com.neojelll.diaxtracker.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import androidx.room.withTransaction
import com.neojelll.diaxtracker.data.DiaryDatabase
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.DiaryEntryProduct
import com.neojelll.diaxtracker.data.MealPresetWithProducts
import java.io.File
import java.time.LocalDateTime
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

    /** What a merge did: entries added / skipped as duplicates, photos restored for existing entries, presets and readings added. */
    data class Summary(
        val entriesAdded: Int,
        val duplicatesSkipped: Int,
        val photosRestored: Int,
        val presetsAdded: Int,
        val readingsAdded: Int
    )

    /** What an archive holds, for showing before the person confirms: entry count and the span they cover. */
    data class ArchiveInfo(val entries: Int, val first: LocalDateTime?, val last: LocalDateTime?)

    private const val TAG = "BackupImporter"
    private const val DB_NAME = "diary_database"
    private const val PHOTOS_DIR_NAME = "entry_photos"
    private const val WORK_DIR_NAME = "backup_import"
    private const val TEMP_DB_NAME = "backup_import_tmp"
    private const val READINGS_PAGE_SIZE = 5000
    private val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)

    suspend fun import(context: Context, source: Uri): ImportResult = withContext(Dispatchers.IO) {
        val workDir = File(context.cacheDir, WORK_DIR_NAME).apply { deleteRecursively(); mkdirs() }
        context.deleteDatabase(TEMP_DB_NAME)
        val copiedPhotos = mutableListOf<File>()
        var entriesCommitted = false
        var backup: DiaryDatabase? = null
        try {
            val extractedDb = extract(context, source, workDir) ?: return@withContext ImportResult.InvalidFile
            val archive = try {
                DiaryDatabase.newBuilder(context, TEMP_DB_NAME).createFromFile(extractedDb).build()
            } catch (e: Exception) {
                Log.w(TAG, "Archive database can't be opened", e)
                return@withContext ImportResult.InvalidFile
            }
            backup = archive

            val backupEntries: List<DiaryEntry>
            val backupProducts: Map<Long, List<DiaryEntryProduct>>
            val backupPresets: List<MealPresetWithProducts>
            try {
                backupEntries = archive.diaryDao().getAllEntries().first()
                backupProducts = archive.diaryDao().getAllEntryProducts().first().groupBy { it.diaryEntryId }
                backupPresets = archive.mealPresetDao().getAllPresetsWithProducts().first()
            } catch (e: Exception) {
                Log.w(TAG, "Archive database can't be read", e)
                return@withContext ImportResult.InvalidFile
            }

            val live = DiaryDatabase.getDatabase(context)
            val photosDir = File(context.filesDir, PHOTOS_DIR_NAME).apply { mkdirs() }
            val archivePhotos = File(workDir, PHOTOS_DIR_NAME)
            var added = 0
            var skipped = 0
            var photosRestored = 0
            var presetsAdded = 0

            live.withTransaction {
                val diaryDao = live.diaryDao()
                // Backup id -> id in the live database, so an automatic check keeps pointing at its
                // meal. Meals come before their checks (a check is always later), hence the order.
                val liveIds = HashMap<Long, Long>()
                for (backupEntry in backupEntries.sortedBy { it.createdAt }) {
                    val entry = backupEntry.withSourceIn(liveIds)
                    val duplicate = findDuplicateEntry(diaryDao.getEntriesAt(entry.createdAt), entry)
                    if (duplicate != null) {
                        liveIds[backupEntry.id] = duplicate.id
                        skipped++
                        // The record is already here but its photo is gone (deleted, or lost earlier):
                        // the archive can give it back.
                        if (duplicate.photoPath?.let { File(it).exists() } != true) {
                            adoptPhoto(entry.photoPath, archivePhotos, photosDir)?.let { restored ->
                                copiedPhotos += File(restored)
                                diaryDao.update(duplicate.copy(photoPath = restored))
                                photosRestored++
                            }
                        }
                        continue
                    }
                    val photoPath = adoptPhoto(entry.photoPath, archivePhotos, photosDir)
                        ?.also { copiedPhotos += File(it) }
                    liveIds[backupEntry.id] = diaryDao.insertWithProducts(
                        entry.copy(id = 0, photoPath = photoPath),
                        backupProducts[backupEntry.id].orEmpty().map { it.copy(id = 0, diaryEntryId = 0) }
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
            entriesCommitted = true

            // Auxiliary data: the entries are already in, so a problem here must not turn the whole
            // import into a reported failure (or undo anything that was committed).
            val readingsAdded = try {
                mergeSensorReadings(archive, live)
            } catch (e: Exception) {
                Log.e(TAG, "Merging the sensor reading log failed", e)
                0
            }
            ImportResult.Success(Summary(added, skipped, photosRestored, presetsAdded, readingsAdded))
        } catch (e: Exception) {
            Log.e(TAG, "Backup import failed", e)
            // Only while the entries transaction hadn't committed: then it rolled back and the photo
            // files copied for those entries are orphans. After a commit the entries own them.
            if (!entriesCommitted) copiedPhotos.forEach { it.delete() }
            ImportResult.Failure
        } finally {
            backup?.close()
            context.deleteDatabase(TEMP_DB_NAME)
            workDir.deleteRecursively()
        }
    }

    /** Reads just the database out of the archive and summarizes it; null if it isn't a readable backup. */
    suspend fun inspect(context: Context, source: Uri): ArchiveInfo? = withContext(Dispatchers.IO) {
        val workDir = File(context.cacheDir, "$WORK_DIR_NAME-preview").apply { deleteRecursively(); mkdirs() }
        try {
            val dbFile = File(workDir, DB_NAME)
            val stream = context.contentResolver.openInputStream(source) ?: return@withContext null
            stream.use { input ->
                ZipInputStream(input).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        if (entry.name == DB_NAME) {
                            dbFile.outputStream().use { zip.copyTo(it) }
                            break
                        }
                    }
                }
            }
            if (!hasSqliteHeader(dbFile)) return@withContext null
            SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
                db.rawQuery("SELECT COUNT(*), MIN(createdAt), MAX(createdAt) FROM diary_entries", null).use { cursor ->
                    cursor.moveToFirst()
                    ArchiveInfo(
                        entries = cursor.getInt(0),
                        first = cursor.getString(1)?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() },
                        last = cursor.getString(2)?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Archive can't be inspected", e)
            null
        } finally {
            workDir.deleteRecursively()
        }
    }

    private fun hasSqliteHeader(file: File): Boolean {
        if (!file.exists() || file.length() < SQLITE_HEADER.size) return false
        val header = ByteArray(SQLITE_HEADER.size).also { buffer -> file.inputStream().use { it.read(buffer) } }
        return header.contentEquals(SQLITE_HEADER)
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
        return dbFile.takeIf { hasSqliteHeader(it) }
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
     * The reading log can hold hundreds of thousands of rows, so it is copied a page at a time (by id)
     * and each page only looks up the live timestamps in its own narrow range.
     */
    private suspend fun mergeSensorReadings(backup: DiaryDatabase, live: DiaryDatabase): Int {
        val backupLog = backup.sensorReadingLogDao()
        val liveLog = live.sensorReadingLogDao()
        var afterId = 0L
        var inserted = 0
        while (true) {
            val page = backupLog.getPage(afterId, READINGS_PAGE_SIZE)
            if (page.isEmpty()) break
            afterId = page.last().id
            val existing = liveLog.getReadingsBetween(page.minOf { it.timestamp }, page.maxOf { it.timestamp })
                .mapTo(HashSet()) { it.timestamp }
            val missing = page.distinctBy { it.timestamp }.filter { it.timestamp !in existing }.map { it.copy(id = 0) }
            if (missing.isNotEmpty()) {
                liveLog.insertAll(missing)
                inserted += missing.size
            }
        }
        return inserted
    }
}

/**
 * The existing entry that is the same record as [candidate]: same moment, same values. Different
 * values at the same moment are a genuine conflict and both are kept, so importing never loses
 * data. The photo path is ignored: it points into a different app data folder in the archive.
 */
/**
 * The entry with its meal link translated through [liveIds]; a link to a meal that isn't in the
 * archive is dropped, so it can't point at some unrelated live entry.
 */
internal fun DiaryEntry.withSourceIn(liveIds: Map<Long, Long>): DiaryEntry {
    val source = sourceEntryId ?: return this
    val mapped = liveIds[source] ?: return copy(sourceEntryId = null, sourceHour = null)
    return copy(sourceEntryId = mapped)
}

internal fun findDuplicateEntry(existing: List<DiaryEntry>, candidate: DiaryEntry): DiaryEntry? {
    val normalized = candidate.copy(id = 0, photoPath = null)
    return existing.firstOrNull { it.copy(id = 0, photoPath = null) == normalized }
}
