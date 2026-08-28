package com.nexusai.domain.ai

import kotlinx.coroutines.flow.Flow

interface AIProvider {
    val id: String
    val name: String
    val type: String
    val supportsStreaming: Boolean

    suspend fun sendMessage(
        messages: List<ChatMessage>,
        model: String,
        maxTokens: Int = 4096,
        temperature: Float = 0.7f
    ): AIResponse

    fun sendMessageStream(
        messages: List<ChatMessage>,
        model: String,
        maxTokens: Int = 4096,
        temperature: Float = 0.7f
    ): Flow<String>

    suspend fun listModels(): List<String>
}

data class ChatMessage(
    val role: MessageRole,
    val content: String
)

enum class MessageRole {
    SYSTEM,
    USER,
    ASSISTANT
}

data class AIResponse(
    val content: String,
    val model: String,
    val usage: TokenUsage? = null
)

data class TokenUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0
)
