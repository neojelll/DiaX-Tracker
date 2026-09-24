package com.neojelll.diaxtracker.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import com.neojelll.diaxtracker.data.DiaryDatabase
import java.io.File
import java.time.LocalDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Archive layout is unchanged from before: the SQLite file `diary_database` plus `entry_photos/`.
 * A period export is the same file with the rows outside the period deleted from a private copy, so
 * every backup - old or new, full or filtered - stays readable by BackupImporter the same way.
 */
object BackupExporter {
    private const val TAG = "BackupExporter"
    private const val DB_NAME = "diary_database"
    private const val PHOTOS_DIR_NAME = "entry_photos"
    private const val WORK_DIR_NAME = "backup_export"

    /** [from] null exports everything; otherwise only entries and sensor readings at or after it. */
    suspend fun export(context: Context, destination: Uri, from: LocalDateTime?): Boolean = withContext(Dispatchers.IO) {
        val workDir = File(context.cacheDir, WORK_DIR_NAME).apply { deleteRecursively(); mkdirs() }
        try {
            // Flushes the write-ahead log into the main DB file so it alone is a complete,
            // self-contained snapshot (no separate -wal/-shm to lose track of).
            DiaryDatabase.getDatabase(context).openHelper.writableDatabase
                .query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }

            val snapshot = File(workDir, DB_NAME)
            context.getDatabasePath(DB_NAME).copyTo(snapshot, overwrite = true)
            val photoNames = trimAndListPhotos(snapshot, from)

            val photosDir = File(context.filesDir, PHOTOS_DIR_NAME)
            val stream = context.contentResolver.openOutputStream(destination) ?: return@withContext false
            stream.use { out ->
                ZipOutputStream(out).use { zip ->
                    zip.putNextEntry(ZipEntry(DB_NAME))
                    snapshot.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                    photoNames.forEach { name ->
                        val photo = File(photosDir, name)
                        if (!photo.exists()) return@forEach
                        zip.putNextEntry(ZipEntry("$PHOTOS_DIR_NAME/$name"))
                        photo.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Backup export failed", e)
            false
        } finally {
            workDir.deleteRecursively()
        }
    }

    /** Deletes everything before [from] from the private copy (if given) and returns the photo files it still references. */
    private fun trimAndListPhotos(snapshot: File, from: LocalDateTime?): Set<String> {
        SQLiteDatabase.openDatabase(snapshot.path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            if (from != null) {
                val cutoff = arrayOf(from.toString())
                db.beginTransaction()
                try {
                    db.execSQL("DELETE FROM diary_entries WHERE createdAt < ?", cutoff)
                    db.execSQL("DELETE FROM diary_entry_products WHERE diaryEntryId NOT IN (SELECT id FROM diary_entries)")
                    db.execSQL("DELETE FROM sensor_readings_log WHERE timestamp < ?", cutoff)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                // Deleted rows leave free pages behind; without this the file would stay full-size.
                db.execSQL("VACUUM")
                db.rawQuery("PRAGMA wal_checkpoint(TRUNCATE)", null).use { it.moveToFirst() }
            }
            return db.rawQuery("SELECT photoPath FROM diary_entries WHERE photoPath IS NOT NULL", null).use { cursor ->
                buildSet { while (cursor.moveToNext()) add(File(cursor.getString(0)).name) }
            }
        }
    }
}
