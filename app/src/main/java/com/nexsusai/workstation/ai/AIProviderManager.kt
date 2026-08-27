package com.nexsusai.workstation.ai

interface AIProviderManager {
    suspend fun sendMessage(
        model: AIModel,
        message: String,
        context: String?
    ): String
}

class DefaultAIProviderManager : AIProviderManager {
    override suspend fun sendMessage(
        model: AIModel,
        message: String,
        context: String?
    ): String {
        return "AI provider placeholder response for ${model.name}"
    }
}
