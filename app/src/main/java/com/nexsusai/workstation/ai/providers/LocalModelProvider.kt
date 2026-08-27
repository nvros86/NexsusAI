package com.nexsusai.workstation.ai.providers

import com.nexsusai.workstation.ai.StreamingAIProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LocalModelProvider : StreamingAIProvider {
    override val id: String = "local"

    override fun stream(prompt: String, apiKey: String): Flow<String> = flow {
        emit("Local model provider foundation ready.")
    }
}
