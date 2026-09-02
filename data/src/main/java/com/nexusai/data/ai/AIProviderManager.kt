package com.nexusai.data.ai

import com.nexusai.data.ai.anthropic.AnthropicProvider
import com.nexusai.data.ai.openai.OpenAIProvider
import com.nexusai.data.security.ApiKeyEncryption
import com.nexusai.domain.ai.AIProvider
import com.nexusai.domain.ai.AIProviderFactory
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.ProviderType
import io.ktor.client.HttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIProviderManager @Inject constructor(
    private val encryption: ApiKeyEncryption,
    private val httpClient: HttpClient
) : AIProviderFactory {
    private val providers = mutableMapOf<String, AIProvider>()

    override fun getProvider(config: AIProviderConfig): AIProvider {
        return providers[config.id] ?: createProvider(config).also {
            providers[config.id] = it
        }
    }

    private fun createProvider(config: AIProviderConfig): AIProvider {
        val apiKey = if (config.apiKey.isNotEmpty()) {
            try {
                encryption.decrypt(config.apiKey)
            } catch (e: Exception) {
                config.apiKey
            }
        } else {
            ""
        }

        return when (config.type) {
            ProviderType.OPENAI -> OpenAIProvider(
                httpClient = httpClient,
                apiKey = apiKey,
                baseUrl = config.baseUrl.ifEmpty { "https://api.openai.com/v1" }
            )
            ProviderType.ANTHROPIC -> AnthropicProvider(
                httpClient = httpClient,
                apiKey = apiKey,
                baseUrl = config.baseUrl.ifEmpty { "https://api.anthropic.com/v1" }
            )
            else -> throw UnsupportedOperationException("Provider ${config.type} not implemented yet")
        }
    }

    fun removeProvider(id: String) {
        providers.remove(id)
    }

    fun clearCache() {
        providers.clear()
    }
}
