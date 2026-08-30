package com.nexusai.data.ai

import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.AIRoutingResult
import com.nexusai.domain.model.ProviderScore
import com.nexusai.domain.model.RoutingStrategy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRouter @Inject constructor() {

    fun route(
        providers: List<AIProviderConfig>,
        strategy: RoutingStrategy,
        taskHint: String = ""
    ): AIRoutingResult? {
        if (providers.isEmpty()) return null

        val scored = providers.map { scoreProvider(it, strategy, taskHint) }
            .sortedByDescending { it.totalScore }

        val selected = scored.firstOrNull() ?: return null
        val failoverChain = scored.drop(1).take(2).map { it.provider }

        val model = selectModel(selected.provider, taskHint)
        val reason = buildReason(selected, strategy)

        return AIRoutingResult(
            selectedProvider = selected.provider,
            selectedModel = model,
            strategy = strategy,
            score = selected.totalScore,
            failoverChain = failoverChain,
            reason = reason
        )
    }

    private fun scoreProvider(
        provider: AIProviderConfig,
        strategy: RoutingStrategy,
        taskHint: String
    ): ProviderScore {
        val hasKey = provider.apiKey.isNotEmpty()
        val availability = if (hasKey) 1.0f else 0.0f

        val quality = when (provider.type.name) {
            "ANTHROPIC" -> 0.95f
            "OPENAI" -> 0.90f
            "GEMINI" -> 0.85f
            "LOCAL" -> 0.50f
            else -> 0.60f
        }

        val speed = when (provider.type.name) {
            "OPENAI" -> 0.85f
            "ANTHROPIC" -> 0.80f
            "GEMINI" -> 0.90f
            "LOCAL" -> 0.95f
            else -> 0.70f
        }

        val cost = when (provider.type.name) {
            "ANTHROPIC" -> 0.40f
            "OPENAI" -> 0.50f
            "GEMINI" -> 0.70f
            "LOCAL" -> 1.00f
            else -> 0.60f
        }

        val totalScore = when (strategy) {
            RoutingStrategy.BEST_QUALITY -> (quality * 0.7f + speed * 0.2f + cost * 0.1f) * availability
            RoutingStrategy.CHEAPEST -> (cost * 0.7f + quality * 0.2f + speed * 0.1f) * availability
            RoutingStrategy.FASTEST -> (speed * 0.7f + quality * 0.2f + cost * 0.1f) * availability
            RoutingStrategy.BALANCED -> (quality * 0.34f + speed * 0.33f + cost * 0.33f) * availability
            RoutingStrategy.FALLBACK_ONLY -> availability
        }

        return ProviderScore(
            provider = provider,
            qualityScore = quality,
            speedScore = speed,
            costScore = cost,
            availabilityScore = availability,
            totalScore = totalScore
        )
    }

    private fun selectModel(provider: AIProviderConfig, taskHint: String): String {
        if (provider.models.isNotEmpty()) {
            return provider.defaultModel.ifEmpty { provider.models.first() }
        }
        return when (provider.type.name) {
            "OPENAI" -> "gpt-4o"
            "ANTHROPIC" -> "claude-sonnet-4-20250514"
            "GEMINI" -> "gemini-2.5-pro"
            else -> "default"
        }
    }

    private fun buildReason(scored: ProviderScore, strategy: RoutingStrategy): String {
        return when (strategy) {
            RoutingStrategy.BEST_QUALITY -> "Выбран ${scored.provider.name} (качество: ${scored.qualityScore})"
            RoutingStrategy.CHEAPEST -> "Выбран ${scored.provider.name} (стоимость: ${scored.costScore})"
            RoutingStrategy.FASTEST -> "Выбран ${scored.provider.name} (скорость: ${scored.speedScore})"
            RoutingStrategy.BALANCED -> "Выбран ${scored.provider.name} (баланс: ${scored.totalScore})"
            RoutingStrategy.FALLBACK_ONLY -> "Основной: ${scored.provider.name}"
        }
    }
}
