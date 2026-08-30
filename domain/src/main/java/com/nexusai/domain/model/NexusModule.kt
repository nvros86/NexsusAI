package com.nexusai.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.ui.graphics.vector.ImageVector

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
    val icon: ImageVector,
    val version: String = "1.0.0",
    val isBuiltIn: Boolean = true,
    val isRequired: Boolean = false,
    val isEnabled: Boolean = true,
    val route: String? = null,
    val capabilities: List<String> = emptyList()
)
