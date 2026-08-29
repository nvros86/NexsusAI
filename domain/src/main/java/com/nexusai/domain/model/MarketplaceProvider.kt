package com.nexusai.domain.model

data class MarketplaceProvider(
    val id: String,
    val name: String,
    val description: String,
    val type: ProviderType,
    val baseUrl: String,
    val models: List<String>,
    val defaultModel: String,
    val maxTokens: Int = 4096,
    val temperature: Float = 0.7f,
    val category: MarketplaceCategory,
    val capabilities: List<ProviderCapability>,
    val logoEmoji: String,
    val websiteUrl: String = "",
    val isAdded: Boolean = false
)

enum class MarketplaceCategory(val displayName: String, val emoji: String) {
    TEXT("Текстовые модели", "💬"),
    IMAGE("Генерация изображений", "🎨"),
    VOICE("Голос и аудио", "🎙️"),
    VIDEO("Видео", "🎬"),
    CODE("Код", "💻"),
    SEARCH("Поиск", "🔍")
}

enum class ProviderCapability(val displayName: String) {
    TEXT_GENERATION("Генерация текста"),
    IMAGE_GENERATION("Генерация изображений"),
    CODE_GENERATION("Генерация кода"),
    FILE_ANALYSIS("Анализ файлов"),
    STREAMING("Стриминг"),
    FUNCTION_CALLING("Вызов функций"),
    VISION("Видение"),
    VOICE("Голос"),
    SEARCH("Поиск в интернете")
}
