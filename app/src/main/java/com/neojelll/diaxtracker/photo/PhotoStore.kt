package com.neojelll.diaxtracker.photo

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.util.UUID

object PhotoStore {
    private const val TAG = "PhotoStore"
    private const val DIR_NAME = "entry_photos"

    fun savePhoto(context: Context, sourceUri: Uri): String? {
        val dir = File(context.filesDir, DIR_NAME).apply { mkdirs() }
        val destFile = File(dir, "${UUID.randomUUID()}.jpg")
        return try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            destFile.absolutePath
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save photo from $sourceUri", e)
            destFile.delete()
            null
        }
    }

    fun deletePhoto(path: String?) {
        if (path == null) return
        File(path).delete()
    }
}
