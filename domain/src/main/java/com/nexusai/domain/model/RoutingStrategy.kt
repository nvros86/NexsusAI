package com.nexusai.domain.model

enum class RoutingStrategy(val displayName: String, val description: String) {
    BEST_QUALITY("Лучшее качество", "Приоритет самым мощным моделям"),
    CHEAPEST("Самый дешёвый", "Минимизация стоимости запросов"),
    FASTEST("Самый быстрый", "Приоритет скорости отклика"),
    BALANCED("Сбалансированный", "Баланс качества, скорости и стоимости"),
    FALLBACK_ONLY("Только failover", "Основной провайдер, при ошибке — запасной")
}

data class AIRoutingResult(
    val selectedProvider: AIProviderConfig,
    val selectedModel: String,
    val strategy: RoutingStrategy,
    val score: Float,
    val failoverChain: List<AIProviderConfig>,
    val reason: String
)

data class ProviderScore(
    val provider: AIProviderConfig,
    val qualityScore: Float,
    val speedScore: Float,
    val costScore: Float,
    val availabilityScore: Float,
    val totalScore: Float
)
