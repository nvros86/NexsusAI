package com.nexusai.core.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object ShareHelper {
    fun shareText(context: Context, title: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, title)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun shareConversation(context: Context, title: String, messages: List<Pair<String, String>>) {
        val formatted = buildString {
            appendLine("# $title")
            appendLine()
            messages.forEach { (role, content) ->
                appendLine("## $role")
                appendLine(content)
                appendLine()
            }
        }
        shareText(context, title, formatted)
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String = "Поделиться файлом") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "Не удалось поделиться файлом", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareUri(context: Context, uri: Uri, mimeType: String, title: String = "Поделиться") {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "Не удалось поделиться", Toast.LENGTH_SHORT).show()
        }
    }
}
