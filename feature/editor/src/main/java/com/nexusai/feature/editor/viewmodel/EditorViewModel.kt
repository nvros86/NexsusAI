package com.nexusai.feature.editor.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class EditorState(
    val content: String = "",
    val fileName: String = "",
    val filePath: String = "",
    val isModified: Boolean = false,
    val isReadOnly: Boolean = false,
    val language: String = "text",
    val lineCount: Int = 0,
    val cursorLine: Int = 1,
    val cursorColumn: Int = 1
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    fun updateContent(content: String) {
        _state.value = _state.value.copy(
            content = content,
            isModified = true,
            lineCount = content.lines().size
        )
    }

    fun updateCursor(line: Int, column: Int) {
        _state.value = _state.value.copy(
            cursorLine = line,
            cursorColumn = column
        )
    }

    fun loadFromUri(uri: Uri) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val content = inputStream?.bufferedReader()?.readText() ?: ""
            inputStream?.close()

            val fileName = getFileName(uri)

            _state.value = _state.value.copy(
                content = content,
                fileName = fileName,
                filePath = uri.toString(),
                isModified = false,
                language = detectLanguage(fileName),
                lineCount = content.lines().size
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                content = "Error loading file: ${e.message}",
                fileName = "error.txt"
            )
        }
    }

    fun newFile(name: String = "untitled.txt") {
        _state.value = EditorState(
            fileName = name,
            language = detectLanguage(name),
            lineCount = 1
        )
    }

    fun saveToFile(): Boolean {
        return try {
            if (_state.value.filePath.isNotEmpty()) {
                val uri = Uri.parse(_state.value.filePath)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(_state.value.content.toByteArray())
                }
                _state.value = _state.value.copy(isModified = false)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun getFileName(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) it.getString(nameIndex) else "untitled.txt"
            } else "untitled.txt"
        } ?: "untitled.txt"
    }

    private fun detectLanguage(fileName: String): String {
        return when {
            fileName.endsWith(".kt") -> "kotlin"
            fileName.endsWith(".java") -> "java"
            fileName.endsWith(".py") -> "python"
            fileName.endsWith(".js") -> "javascript"
            fileName.endsWith(".ts") -> "typescript"
            fileName.endsWith(".html") || fileName.endsWith(".htm") -> "html"
            fileName.endsWith(".css") -> "css"
            fileName.endsWith(".json") -> "json"
            fileName.endsWith(".xml") -> "xml"
            fileName.endsWith(".md") -> "markdown"
            fileName.endsWith(".yaml") || fileName.endsWith(".yml") -> "yaml"
            fileName.endsWith(".sh") || fileName.endsWith(".bash") -> "shell"
            fileName.endsWith(".sql") -> "sql"
            fileName.endsWith(".gradle") || fileName.endsWith(".gradle.kts") -> "gradle"
            else -> "text"
        }
    }
}
