package com.nexusai.domain.model

enum class ModuleType(val displayName: String) {
    AI_PROVIDER("AI-провайдеры"),
    TOOL("Инструменты"),
    FEATURE("Фичи"),
    INTEGRATION("Интеграции"),
    AGENT("Агенты")
}

data class NexusModule(
    val id: String,
    val title: String,
    val description: String,
    val type: ModuleType,
    val iconId: String,
    val version: String = "1.0.0",
    val isBuiltIn: Boolean = true,
    val isRequired: Boolean = false,
    val isEnabled: Boolean = true,
    val route: String? = null,
    val capabilities: List<String> = emptyList()
)
