package com.nexsusai.workstation.ai.providers

import com.nexsusai.workstation.ai.StreamingAIProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AnthropicProvider : StreamingAIProvider {
    override val id: String = "anthropic"

    override fun stream(prompt: String, apiKey: String): Flow<String> = flow {
        emit("Anthropic provider connected. Streaming response placeholder.")
    }
}
