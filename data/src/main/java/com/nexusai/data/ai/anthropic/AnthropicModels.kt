package com.nexusai.data.ai.anthropic

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnthropicRequest(
    val model: String,
    val messages: List<AnthropicMessage>,
    @SerialName("max_tokens") val maxTokens: Int = 4096,
    val temperature: Float = 0.7f,
    val system: String? = null,
    val stream: Boolean = false
)

@Serializable
data class AnthropicMessage(
    val role: String,
    val content: String
)

@Serializable
data class AnthropicResponse(
    val id: String,
    val content: List<AnthropicContent>,
    val model: String,
    val usage: AnthropicUsage? = null
)

@Serializable
data class AnthropicContent(
    val type: String,
    val text: String = ""
)

@Serializable
data class AnthropicUsage(
    @SerialName("input_tokens") val inputTokens: Int = 0,
    @SerialName("output_tokens") val outputTokens: Int = 0
)

@Serializable
data class AnthropicStreamEvent(
    val type: String,
    val delta: AnthropicDelta? = null
)

@Serializable
data class AnthropicDelta(
    val type: String? = null,
    val text: String? = null
)
