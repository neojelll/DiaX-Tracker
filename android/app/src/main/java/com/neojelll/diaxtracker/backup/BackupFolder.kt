package com.neojelll.diaxtracker.backup

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract

/** Files inside the folder the person picked for automatic backups (a Storage Access Framework tree). */
internal object BackupFolder {
    /** Human-readable path of the folder, e.g. "Documents/DiaX". */
    fun label(tree: Uri): String =
        DocumentsContract.getTreeDocumentId(tree).substringAfter(':', "").ifEmpty { "/" }

    fun createFile(context: Context, tree: Uri, name: String): Uri? {
        val parent = DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))
        return DocumentsContract.createDocument(context.contentResolver, parent, "application/zip", name)
    }

    fun delete(context: Context, file: Uri): Boolean =
        DocumentsContract.deleteDocument(context.contentResolver, file)

    /** Removes the automatic backups beyond the newest [AUTO_BACKUP_KEEP]; never touches other files. */
    fun deleteStale(context: Context, tree: Uri) {
        val files = list(context, tree)
        val doomed = staleAutoBackups(files.keys.toList())
        doomed.forEach { name -> files[name]?.let { delete(context, it) } }
    }

    /** Display name -> document Uri for every file directly in the folder. */
    private fun list(context: Context, tree: Uri): Map<String, Uri> {
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))
        val projection = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME)
        val result = LinkedHashMap<String, Uri>()
        context.contentResolver.query(children, projection, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                result[cursor.getString(1)] = DocumentsContract.buildDocumentUriUsingTree(tree, cursor.getString(0))
            }
        }
        return result
    }
}
