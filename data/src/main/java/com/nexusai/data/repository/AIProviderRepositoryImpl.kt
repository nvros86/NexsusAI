package com.nexusai.data.repository

import com.nexusai.data.local.AIProviderDao
import com.nexusai.data.local.AIProviderEntity
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.ProviderType
import com.nexusai.domain.repository.AIProviderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AIProviderRepositoryImpl @Inject constructor(
    private val dao: AIProviderDao
) : AIProviderRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getAllProviders(): Flow<List<AIProviderConfig>> {
        return dao.getAllProviders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProviderById(id: String): AIProviderConfig? {
        return dao.getProviderById(id)?.toDomain()
    }

    override suspend fun addProvider(provider: AIProviderConfig) {
        dao.insertProvider(provider.toEntity())
    }

    override suspend fun updateProvider(provider: AIProviderConfig) {
        dao.updateProvider(provider.toEntity())
    }

    override suspend fun deleteProvider(id: String) {
        dao.deleteProviderById(id)
    }

    override suspend fun getFavoriteProviders(): List<AIProviderConfig> {
        return dao.getFavoriteProviders().map { it.toDomain() }
    }

    private fun AIProviderEntity.toDomain() = AIProviderConfig(
        id = id,
        name = name,
        type = ProviderType.valueOf(type),
        baseUrl = baseUrl,
        apiKey = "",
        models = json.decodeFromString(modelsJson),
        defaultModel = defaultModel,
        maxTokens = maxTokens,
        temperature = temperature,
        systemPrompt = systemPrompt,
        customHeaders = json.decodeFromString(customHeadersJson),
        supportsImages = supportsImages,
        supportsFiles = supportsFiles,
        supportsStreaming = supportsStreaming,
        isFavorite = isFavorite
    )

    private fun AIProviderConfig.toEntity() = AIProviderEntity(
        id = id,
        name = name,
        type = type.name,
        baseUrl = baseUrl,
        apiKeyEncrypted = apiKey,
        modelsJson = json.encodeToString(models),
        defaultModel = defaultModel,
        maxTokens = maxTokens,
        temperature = temperature,
        systemPrompt = systemPrompt,
        customHeadersJson = json.encodeToString(customHeaders),
        supportsImages = supportsImages,
        supportsFiles = supportsFiles,
        supportsStreaming = supportsStreaming,
        isFavorite = isFavorite
    )
}
