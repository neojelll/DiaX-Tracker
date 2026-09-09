package com.neojelll.diaxtracker.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import com.neojelll.diaxtracker.data.DiaryDatabase
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object BackupExporter {
    private const val TAG = "BackupExporter"
    private const val DB_NAME = "diary_database"
    private const val PHOTOS_DIR_NAME = "entry_photos"

    suspend fun export(context: Context, destination: Uri): Boolean {
        return try {
            // Flushes the write-ahead log into the main DB file so it alone is a complete,
            // self-contained snapshot inside the zip (no separate -wal/-shm to lose track of).
            DiaryDatabase.getDatabase(context).openHelper.writableDatabase
                .query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }

            val dbFile = context.getDatabasePath(DB_NAME)
            val photosDir = File(context.filesDir, PHOTOS_DIR_NAME)
            val stream = context.contentResolver.openOutputStream(destination) ?: return false

            stream.use { out ->
                ZipOutputStream(out).use { zip ->
                    if (dbFile.exists()) {
                        zip.putNextEntry(ZipEntry(DB_NAME))
                        dbFile.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                    photosDir.listFiles()?.forEach { photo ->
                        zip.putNextEntry(ZipEntry("$PHOTOS_DIR_NAME/${photo.name}"))
                        photo.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Backup export failed", e)
            false
        }
    }
}
