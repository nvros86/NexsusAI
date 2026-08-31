package com.nexusai.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NexsusPlugin(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val author: String,
    val iconEmoji: String,
    val capabilities: List<PluginCapability>,
    val isBuiltIn: Boolean = false,
    val isEnabled: Boolean = true,
    val isInstalled: Boolean = true,
    val config: Map<String, String> = emptyMap()
)

@Serializable
enum class PluginCapability(val displayName: String, val emoji: String) {
    CODE_EXECUTION("Выполнение кода", "⚡"),
    FILE_OPERATIONS("Файловые операции", "📁"),
    GIT_INTEGRATION("Git интеграция", "🔀"),
    DOCKER_SUPPORT("Docker поддержка", "🐳"),
    FIREBASE_TOOLS("Firebase инструменты", "🔥"),
    CUSTOM_COMMANDS("Пользовательские команды", "⌨️"),
    AI_ENHANCEMENT("Улучшение AI", "🤖"),
    EXPORT_IMPORT("Экспорт/Импорт", "📦"),
    UI_CUSTOMIZATION("Кастомизация UI", "🎨"),
    API_INTEGRATION("API интеграция", "🌐")
}

data class PluginCommand(
    val id: String,
    val name: String,
    val description: String,
    val usage: String,
    val pluginId: String
)

data class PluginExecutionResult(
    val pluginId: String,
    val commandId: String,
    val success: Boolean,
    val output: String,
    val error: String? = null,
    val durationMs: Long = 0
)
