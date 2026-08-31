package com.nexusai.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AutomationChain(
    val id: String,
    val name: String,
    val description: String,
    val steps: List<ChainStep> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val runCount: Int = 0
)

@Serializable
data class ChainStep(
    val id: String,
    val type: ChainStepType,
    val name: String,
    val prompt: String,
    val providerType: ProviderType? = null,
    val model: String = "",
    val outputKey: String = "",
    val isEnabled: Boolean = true
)

@Serializable
enum class ChainStepType(val displayName: String, val emoji: String) {
    TEXT_GENERATION("Генерация текста", "✍️"),
    IMAGE_GENERATION("Генерация изображения", "🖼️"),
    VIDEO_GENERATION("Генерация видео", "🎬"),
    CODE_GENERATION("Генерация кода", "💻"),
    SUMMARIZATION("Суммаризация", "📝"),
    TRANSLATION("Перевод", "🌐"),
    CUSTOM("Пользовательский", "⚙️")
}

data class ChainRunResult(
    val chainId: String,
    val stepResults: List<ChainStepResult>,
    val completedAt: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val errorMessage: String? = null
)

data class ChainStepResult(
    val stepId: String,
    val stepName: String,
    val input: String,
    val output: String,
    val durationMs: Long = 0,
    val isError: Boolean = false,
    val errorMessage: String? = null
)
