package com.nexusai.domain.repository

import com.nexusai.domain.model.MarketplaceProvider
import kotlinx.coroutines.flow.Flow

interface MarketplaceRepository {
    fun getAllPresets(): Flow<List<MarketplaceProvider>>
    fun getPresetsByCategory(category: String): Flow<List<MarketplaceProvider>>
    fun searchPresets(query: String): Flow<List<MarketplaceProvider>>
    suspend fun getAddedProviderIds(): Set<String>
    suspend fun markAsAdded(id: String)
}
