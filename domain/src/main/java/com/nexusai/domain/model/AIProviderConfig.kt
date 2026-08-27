package com.nexusai.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AIProviderConfig(
    val id: String,
    val name: String,
    val type: ProviderType,
    val baseUrl: String,
    val apiKey: String = "",
    val models: List<String> = emptyList(),
    val defaultModel: String = "",
    val maxTokens: Int = 4096,
    val temperature: Float = 0.7f,
    val systemPrompt: String = "",
    val customHeaders: Map<String, String> = emptyMap(),
    val supportsImages: Boolean = false,
    val supportsFiles: Boolean = false,
    val supportsStreaming: Boolean = true,
    val isFavorite: Boolean = false
)

@Serializable
enum class ProviderType {
    OPENAI,
    ANTHROPIC,
    GEMINI,
    STABILITY,
    ELEVENLABS,
    RUNWAY,
    CUSTOM,
    LOCAL
}
