package com.nexsusai.workstation.ai.network

interface OpenAIService {
    suspend fun createChatCompletion(request: OpenAIRequest): OpenAIResponse
}

data class OpenAIRequest(
    val model: String,
    val messages: List<OpenAIMessage>,
    val stream: Boolean = true
)

data class OpenAIMessage(
    val role: String,
    val content: String
)

data class OpenAIResponse(
    val content: String
)
