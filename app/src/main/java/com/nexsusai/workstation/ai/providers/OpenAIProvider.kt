package com.nexsusai.workstation.ai.providers

import com.nexsusai.workstation.ai.StreamingAIProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OpenAIProvider : StreamingAIProvider {
    override val id: String = "openai"

    override fun stream(prompt: String, apiKey: String): Flow<String> = flow {
        emit("OpenAI provider connected. Streaming response placeholder.")
    }
}
