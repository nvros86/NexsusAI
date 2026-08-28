package com.nexusai.core.ui.components.file

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

data class FileInfo(
    val name: String,
    val uri: String,
    val mimeType: String,
    val size: Long
)

object FileHelper {

    fun getFileInfo(context: Context, uri: Uri): FileInfo? {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    val name = if (nameIndex >= 0) it.getString(nameIndex) else "unknown"
                    val size = if (sizeIndex >= 0) it.getLong(sizeIndex) else 0L
                    val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

                    FileInfo(
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

    fun isImage(mimeType: String): Boolean {
        return mimeType.startsWith("image/")
    }

    fun isText(mimeType: String): Boolean {
        return mimeType.startsWith("text/") ||
                mimeType.contains("json") ||
                mimeType.contains("xml") ||
                mimeType.contains("javascript") ||
                mimeType.contains("csv")
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}
