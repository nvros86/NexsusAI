package com.nexusai.domain.ai

import com.nexusai.domain.model.AIProviderConfig

interface AIProviderFactory {
    fun getProvider(config: AIProviderConfig): AIProvider
}
