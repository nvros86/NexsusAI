package com.nexusai.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.app.R
import com.nexusai.domain.model.Message
import com.nexusai.domain.model.MessageRole
import com.nexusai.domain.model.Tab
import com.nexusai.domain.repository.TabRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class ExportFormat(val extension: String, val mimeType: String, val displayNameRes: Int) {
    MARKDOWN(".md", "text/markdown", R.string.export_format_markdown),
    TXT(".txt", "text/plain", R.string.export_format_txt),
    JSON(".json", "application/json", R.string.export_format_json),
    HTML(".html", "text/html", R.string.export_format_html)
}

data class ExportUiState(
    val tabs: List<Tab> = emptyList(),
    val selectedTabId: String? = null,
    val selectedFormat: ExportFormat = ExportFormat.MARKDOWN,
    val isExporting: Boolean = false,
    val exportedFileUri: Uri? = null,
    val error: String? = null
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val tabRepository: TabRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tabRepository.getAllTabs().collect { tabs ->
                _uiState.value = _uiState.value.copy(tabs = tabs)
            }
        }
    }

    fun selectTab(tabId: String) {
        _uiState.value = _uiState.value.copy(selectedTabId = tabId)
    }

    fun selectFormat(format: ExportFormat) {
        _uiState.value = _uiState.value.copy(selectedFormat = format)
    }

    fun export(context: Context) {
        val state = _uiState.value
        val tab = state.tabs.find { it.id == state.selectedTabId } ?: return

        _uiState.value = state.copy(isExporting = true, error = null)

        viewModelScope.launch {
            try {
                val content = when (state.selectedFormat) {
                    ExportFormat.MARKDOWN -> toMarkdown(tab)
                    ExportFormat.TXT -> toPlainText(tab)
                    ExportFormat.JSON -> toJson(tab)
                    ExportFormat.HTML -> toHtml(tab)
                }

                val fileName = "nexsusai_${tab.title.replace("\\s+".toRegex(), "_")}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}${state.selectedFormat.extension}"

                val exportDir = File(context.cacheDir, "exports")
                exportDir.mkdirs()
                val file = File(exportDir, fileName)
                file.writeText(content)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportedFileUri = uri
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = "Ошибка экспорта: ${e.message ?: ""}"
                )
            }
        }
    }

    fun share(context: Context) {
        val state = _uiState.value
        val uri = state.exportedFileUri ?: return
        val tab = state.tabs.find { it.id == state.selectedTabId } ?: return

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = state.selectedFormat.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "NexsusAI - ${tab.title}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Поделиться"))
    }

    fun clearExport() {
        _uiState.value = _uiState.value.copy(exportedFileUri = null)
    }

    fun copyToClipboard(context: Context) {
        val state = _uiState.value
        val tab = state.tabs.find { it.id == state.selectedTabId } ?: return

        viewModelScope.launch {
            try {
                val content = when (state.selectedFormat) {
                    ExportFormat.MARKDOWN -> toMarkdown(tab)
                    ExportFormat.TXT -> toPlainText(tab)
                    ExportFormat.JSON -> toJson(tab)
                    ExportFormat.HTML -> toHtml(tab)
                }

                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText(tab.title, content)
                clipboard.setPrimaryClip(clip)
            } catch (_: Exception) {}
        }
    }

    fun openInBrowser(context: Context) {
        val uri = _uiState.value.exportedFileUri ?: return
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "text/html")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    private fun toMarkdown(tab: Tab): String = buildString {
        appendLine("# ${tab.title}")
        appendLine()
        appendLine("*Экспортировано из NexsusAI — ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())}*")
        appendLine()
        appendLine("---")
        appendLine()
        tab.messages.forEach { msg ->
            val role = when (msg.role) {
                MessageRole.USER -> "Пользователь"
                MessageRole.ASSISTANT -> "Ассистент"
                MessageRole.SYSTEM -> "Система"
            }
            appendLine("### $role")
            appendLine()
            appendLine(msg.content)
            appendLine()
            if (msg.attachments.isNotEmpty()) {
                appendLine("**Вложения** ${msg.attachments.joinToString(", ") { it.name }}")
                appendLine()
            }
        }
    }

    private fun toPlainText(tab: Tab): String = buildString {
        appendLine("${tab.title}")
        appendLine("=".repeat(tab.title.length))
        appendLine()
        tab.messages.forEach { msg ->
            val role = when (msg.role) {
                MessageRole.USER -> "Пользователь"
                MessageRole.ASSISTANT -> "Ассистент"
                MessageRole.SYSTEM -> "Система"
            }
            appendLine("[$role]")
            appendLine(msg.content)
            appendLine()
        }
    }

    private fun toJson(tab: Tab): String {
        val json = buildJsonObject {
            put("title", tab.title)
            put("exportedAt", System.currentTimeMillis())
            put("provider", tab.aiProviderId ?: "unknown")
            put("messages", buildJsonArray {
                tab.messages.forEach { msg ->
                    add(buildJsonObject {
                        put("role", msg.role.name.lowercase())
                        put("content", msg.content)
                        put("timestamp", msg.timestamp)
                        if (msg.attachments.isNotEmpty()) {
                            put("attachments", buildJsonArray {
                                msg.attachments.forEach { att ->
                                    add(buildJsonObject {
                                        put("name", att.name)
                                        put("mimeType", att.mimeType)
                                        put("size", att.size)
                                    })
                                }
                            })
                        }
                    })
                }
            })
        }
        return Json { prettyPrint = true }.encodeToString(JsonObject.serializer(), json)
    }

    private fun toHtml(tab: Tab): String = buildString {
        appendLine("<!DOCTYPE html>")
        appendLine("<html lang=\"ru\">")
        appendLine("<head>")
        appendLine("<meta charset=\"UTF-8\">")
        appendLine("<title>${tab.title}</title>")
        appendLine("<style>")
        appendLine("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; background: #1a1a2e; color: #e0e0e0; }")
        appendLine("h1 { color: #bb86fc; }")
        appendLine(".message { margin: 16px 0; padding: 12px; border-radius: 8px; }")
        appendLine(".user { background: #2d2d44; }")
        appendLine(".assistant { background: #1e1e3a; border-left: 3px solid #bb86fc; }")
        appendLine(".role { font-weight: bold; color: #bb86fc; margin-bottom: 8px; }")
        appendLine(".content { white-space: pre-wrap; }")
        appendLine("code { background: #333; padding: 2px 6px; border-radius: 4px; }")
        appendLine("pre { background: #333; padding: 12px; border-radius: 8px; overflow-x: auto; }")
        appendLine("</style>")
        appendLine("</head>")
        appendLine("<body>")
        appendLine("<h1>${tab.title}</h1>")
        appendLine("<p><em>Экспортировано из NexsusAI</em></p>")
        appendLine("<hr>")

        tab.messages.forEach { msg ->
            val roleClass = when (msg.role) {
                MessageRole.USER -> "user"
                MessageRole.ASSISTANT -> "assistant"
                MessageRole.SYSTEM -> "user"
            }
            val roleLabel = when (msg.role) {
                MessageRole.USER -> "Пользователь"
                MessageRole.ASSISTANT -> "Ассистент"
                MessageRole.SYSTEM -> "Система"
            }
            appendLine("<div class=\"message $roleClass\">")
            appendLine("<div class=\"role\">$roleLabel</div>")
            appendLine("<div class=\"content\">${msg.content.replace("<", "&lt;").replace(">", "&gt;")}</div>")
            appendLine("</div>")
        }

        appendLine("</body>")
        appendLine("</html>")
    }
}
