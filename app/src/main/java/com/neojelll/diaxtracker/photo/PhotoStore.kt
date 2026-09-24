package com.neojelll.diaxtracker.photo

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

object PhotoStore {
    private const val TAG = "PhotoStore"
    private const val DIR_NAME = "entry_photos"
    private const val CAPTURE_DIR_NAME = "camera"

    /** Empty temp file plus the FileProvider URI the system camera app writes its capture into. */
    fun newCaptureTarget(context: Context): Pair<File, Uri> {
        val dir = File(context.cacheDir, CAPTURE_DIR_NAME).apply { mkdirs() }
        val file = File.createTempFile("capture_", ".jpg", dir)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return file to uri
    }

    /** Moves a finished camera capture into permanent photo storage; null if the camera wrote nothing. */
    fun saveCapture(context: Context, capture: File): String? {
        if (!capture.exists() || capture.length() == 0L) return null
        val dir = File(context.filesDir, DIR_NAME).apply { mkdirs() }
        val destFile = File(dir, "${UUID.randomUUID()}.jpg")
        return if (capture.renameTo(destFile)) destFile.absolutePath else null
    }

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
