package com.neojelll.diaxtracker.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import com.neojelll.diaxtracker.data.DiaryDatabase
import java.io.File
import java.util.zip.ZipInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BackupImporter {
    enum class ImportResult { SUCCESS, INVALID_FILE, FAILURE }

    private const val TAG = "BackupImporter"
    private const val DB_NAME = "diary_database"
    private const val PHOTOS_DIR_NAME = "entry_photos"
    private val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)

    suspend fun import(context: Context, source: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            var dbBytes: ByteArray? = null
            val photoBytes = mutableMapOf<String, ByteArray>()

            val stream = context.contentResolver.openInputStream(source) ?: return@withContext ImportResult.FAILURE
            stream.use { input ->
                ZipInputStream(input).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        when {
                            entry.name == DB_NAME -> dbBytes = zip.readBytes()
                            entry.name.startsWith("$PHOTOS_DIR_NAME/") ->
                                photoBytes[entry.name.removePrefix("$PHOTOS_DIR_NAME/")] = zip.readBytes()
                        }
                    }
                }
            }

            val db = dbBytes ?: return@withContext ImportResult.INVALID_FILE
            if (db.size < SQLITE_HEADER.size || !db.copyOfRange(0, SQLITE_HEADER.size).contentEquals(SQLITE_HEADER)) {
                return@withContext ImportResult.INVALID_FILE
            }

            // Nothing real is touched above this line - only past this point do we mutate.
            DiaryDatabase.closeAndResetInstance()

            val dbFile = context.getDatabasePath(DB_NAME)
            val tmpFile = File(dbFile.parentFile, "$DB_NAME.tmp")
            tmpFile.writeBytes(db)
            tmpFile.renameTo(dbFile)
            // A stale WAL from the old database must not survive - SQLite would try to replay
            // it against the freshly-swapped main file on next open.
            File(dbFile.path + "-wal").delete()
            File(dbFile.path + "-shm").delete()

            val photosDir = File(context.filesDir, PHOTOS_DIR_NAME)
            photosDir.deleteRecursively()
            photosDir.mkdirs()
            photoBytes.forEach { (name, bytes) -> File(photosDir, name).writeBytes(bytes) }

            ImportResult.SUCCESS
        } catch (e: Exception) {
            Log.e(TAG, "Backup import failed", e)
            ImportResult.FAILURE
        }
    }
}
