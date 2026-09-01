package com.nexusai.data.common

import com.nexusai.data.local.AgentDao
import com.nexusai.data.local.AgentEntity
import com.nexusai.data.local.MemoryEntryDao
import com.nexusai.data.local.MemoryEntryEntity
import com.nexusai.domain.model.AIAgent
import com.nexusai.domain.model.MemoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDataManager @Inject constructor(
    private val agentDao: AgentDao,
    private val memoryEntryDao: MemoryEntryDao
) {
    val agents: Flow<List<AIAgent>> = agentDao.getAllAgents().map { entities ->
        entities.map { it.toDomain() }
    }

    val memoryEntries: Flow<List<MemoryEntry>> = memoryEntryDao.getAllEntries().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getActiveAgentSystemPrompt(): String? {
        val agents = agentDao.getAllAgents().map { list -> list.filter { it.isActive } }
        var result: String? = null
        agents.collect { activeAgents ->
            if (activeAgents.isNotEmpty()) {
                result = activeAgents.joinToString("\n\n") { agent ->
                    "[${agent.name}]: ${agent.systemPrompt}"
                }
            }
        }
        return result
    }

    suspend fun getMemoryContext(): String? {
        val entries = mutableListOf<MemoryEntryEntity>()
        memoryEntryDao.getAllEntries().collect { entries.addAll(it) }
        if (entries.isEmpty()) return null
        val importantEntries = entries.filter { it.isImportant }
        val allEntries = if (importantEntries.isNotEmpty()) importantEntries else entries
        return allEntries.joinToString("\n") { "${it.key}: ${it.value}" }
    }

    suspend fun addAgent(agent: AIAgent) {
        agentDao.insertAgent(agent.toEntity())
    }

    suspend fun updateAgent(agent: AIAgent) {
        agentDao.updateAgent(agent.toEntity())
    }

    suspend fun removeAgent(id: String) {
        agentDao.deleteAgentById(id)
    }

    suspend fun addMemoryEntry(entry: MemoryEntry) {
        memoryEntryDao.insertEntry(entry.toEntity())
    }

    suspend fun updateMemoryEntry(entry: MemoryEntry) {
        memoryEntryDao.updateEntry(entry.toEntity())
    }

    suspend fun removeMemoryEntry(id: String) {
        memoryEntryDao.deleteEntryById(id)
    }

    suspend fun clearMemory() {
        memoryEntryDao.deleteAllEntries()
    }

    private fun AgentEntity.toDomain() = AIAgent(
        id = id, name = name, description = description,
        systemPrompt = systemPrompt, isActive = isActive, createdAt = createdAt
    )

    private fun AIAgent.toEntity() = AgentEntity(
        id = id, name = name, description = description,
        systemPrompt = systemPrompt, isActive = isActive, createdAt = createdAt
    )

    private fun MemoryEntryEntity.toDomain() = MemoryEntry(
        id = id, key = key, value = value,
        isImportant = isImportant, createdAt = createdAt, updatedAt = updatedAt
    )

    private fun MemoryEntry.toEntity() = MemoryEntryEntity(
        id = id, key = key, value = value,
        isImportant = isImportant, createdAt = createdAt, updatedAt = updatedAt
    )
}
