package com.nexusai.domain.model

data class Prompt(
    val id: String,
    val title: String,
    val description: String,
    val content: String,
    val category: PromptCategory,
    val tags: List<String> = emptyList(),
    val usageCount: Int = 0,
    val isFavorite: Boolean = false,
    val language: String = "ru"
)

enum class PromptCategory(val displayName: String, val emoji: String) {
    WRITING("Письмо и тексты", "✍️"),
    CODING("Программирование", "💻"),
    MARKETING("Маркетинг", "📢"),
    BUSINESS("Бизнес", "💼"),
    CREATIVE("Креатив", "🎨"),
    EDUCATION("Обучение", "📚"),
    ANALYSIS("Анализ", "📊"),
    TRANSLATION("Перевод", "🌐"),
    DAILY("Повседневные", "🗓️")
}
