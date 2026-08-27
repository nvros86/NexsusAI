package com.nexusai.domain.repository

import com.nexusai.domain.model.AIProviderConfig
import kotlinx.coroutines.flow.Flow

interface AIProviderRepository {
    fun getAllProviders(): Flow<List<AIProviderConfig>>
    suspend fun getProviderById(id: String): AIProviderConfig?
    suspend fun addProvider(provider: AIProviderConfig)
    suspend fun updateProvider(provider: AIProviderConfig)
    suspend fun deleteProvider(id: String)
    suspend fun getFavoriteProviders(): List<AIProviderConfig>
}
