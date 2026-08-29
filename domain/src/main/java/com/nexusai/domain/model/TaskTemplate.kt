package com.nexusai.domain.model

data class TaskTemplate(
    val id: String,
    val title: String,
    val description: String,
    val category: TemplateCategory,
    val iconEmoji: String,
    val systemPrompt: String,
    val examplePrompt: String,
    val recommendedProviderType: ProviderType? = null,
    val inputPlaceholders: List<InputPlaceholder> = emptyList(),
    val outputFormat: String = "text"
)

data class InputPlaceholder(
    val key: String,
    val label: String,
    val defaultValue: String = "",
    val isRequired: Boolean = true
)

enum class TemplateCategory(val displayName: String, val emoji: String) {
    VIDEO("Видео", "🎬"),
    DESIGN("Дизайн", "🖼️"),
    WEB_DEVELOPMENT("Веб-разработка", "🌐"),
    APP_DEVELOPMENT("Мобильная разработка", "📱"),
    CONTENT("Контент", "✍️"),
    BUSINESS("Бизнес", "💼"),
    EDUCATION("Образование", "📚")
}
