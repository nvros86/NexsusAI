package com.nexsusai.workstation.ai.providers

import kotlinx.coroutines.flow.Flow

interface StreamingAIProvider {
    suspend fun stream(
        model: String,
        prompt: String,
        context: String
    ): Flow<String>
}
