package com.nexsusai.workstation.ai.network

interface AnthropicService {
    suspend fun createMessage(request: AnthropicRequest): AnthropicResponse
}

data class AnthropicRequest(
    val model: String,
    val messages: List<AnthropicMessage>,
    val stream: Boolean = true
)

data class AnthropicMessage(
    val role: String,
    val content: String
)

data class AnthropicResponse(
    val content: String
)
