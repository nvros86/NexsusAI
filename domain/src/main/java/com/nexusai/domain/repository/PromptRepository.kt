package com.nexusai.domain.repository

import com.nexusai.domain.model.Prompt
import kotlinx.coroutines.flow.Flow

interface PromptRepository {
    fun getAllPrompts(): Flow<List<Prompt>>
    fun getPromptsByCategory(category: String): Flow<List<Prompt>>
    fun searchPrompts(query: String): Flow<List<Prompt>>
    fun getFavoritePrompts(): Flow<List<Prompt>>
    suspend fun toggleFavorite(id: String)
    suspend fun incrementUsage(id: String)
}
