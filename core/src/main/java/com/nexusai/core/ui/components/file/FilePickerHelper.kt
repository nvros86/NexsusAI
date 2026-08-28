package com.nexusai.core.ui.components.file

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.nexusai.domain.model.AttachedFile

class FilePickerHelper(
    private val activity: AppCompatActivity,
    private val onFilesSelected: (List<AttachedFile>) -> Unit
) {
    private var imageOnly = false

    private val filePickerLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(
            ActivityResultContracts.OpenMultipleDocuments()
        ) { uris: List<Uri> ->
            val files = uris.mapNotNull { uri ->
                getFileFromUri(activity, uri)
            }
            onFilesSelected(files)
        }

    private val imagePickerLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(
            ActivityResultContracts.OpenMultipleDocuments()
        ) { uris: List<Uri> ->
            val files = uris.mapNotNull { uri ->
                getFileFromUri(activity, uri)
            }
            onFilesSelected(files)
        }

    fun pickFiles() {
        filePickerLauncher.launch(arrayOf("*/*"))
    }

    fun pickImages() {
        imagePickerLauncher.launch(arrayOf("image/*"))
    }

    companion object {
        fun getFileFromUri(context: Context, uri: Uri): AttachedFile? {
            return try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                        val name = if (nameIndex >= 0) it.getString(nameIndex) else "unknown"
                        val size = if (sizeIndex >= 0) it.getLong(sizeIndex) else 0L
                        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

                        AttachedFile(
                            id = uri.toString(),
                            name = name,
                            uri = uri.toString(),
                            mimeType = mimeType,
                            size = size
                        )
                    } else null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}
