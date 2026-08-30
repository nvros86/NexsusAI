package com.nexusai.domain.repository

import com.nexusai.domain.model.NexusModule
import kotlinx.coroutines.flow.Flow

interface ModuleRepository {
    fun getAllModules(): Flow<List<NexusModule>>
    fun getEnabledModules(): Flow<List<NexusModule>>
    fun getModulesByType(type: String): Flow<List<NexusModule>>
    fun searchModules(query: String): Flow<List<NexusModule>>
    suspend fun setModuleEnabled(id: String, enabled: Boolean)
    suspend fun isModuleEnabled(id: String): Boolean
}
