package com.nexusai.core.common

import com.nexusai.domain.model.AIAgent
import com.nexusai.domain.model.MemoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDataManager @Inject constructor() {

    private val _agents = MutableStateFlow<List<AIAgent>>(emptyList())
    val agents: StateFlow<List<AIAgent>> = _agents.asStateFlow()

    private val _memoryEntries = MutableStateFlow<List<MemoryEntry>>(emptyList())
    val memoryEntries: StateFlow<List<MemoryEntry>> = _memoryEntries.asStateFlow()

    fun getActiveAgents(): List<AIAgent> = _agents.value.filter { it.isActive }

    fun getActiveAgentSystemPrompt(): String? {
        val activeAgents = getActiveAgents()
        return if (activeAgents.isNotEmpty()) {
            activeAgents.joinToString("\n\n") { agent ->
                "[${agent.name}]: ${agent.systemPrompt}"
            }
        } else null
    }

    fun getMemoryContext(): String? {
        val entries = _memoryEntries.value
        return if (entries.isNotEmpty()) {
            val importantEntries = entries.filter { it.isImportant }
            val allEntries = if (importantEntries.isNotEmpty()) importantEntries else entries
            allEntries.joinToString("\n") { "${it.key}: ${it.value}" }
        } else null
    }

    fun addAgent(agent: AIAgent) {
        _agents.update { it + agent }
    }

    fun updateAgent(agent: AIAgent) {
        _agents.update { list -> list.map { if (it.id == agent.id) agent else it } }
    }

    fun removeAgent(id: String) {
        _agents.update { it.filter { a -> a.id != id } }
    }

    fun addMemoryEntry(entry: MemoryEntry) {
        _memoryEntries.update { it + entry }
    }

    fun updateMemoryEntry(entry: MemoryEntry) {
        _memoryEntries.update { list -> list.map { if (it.id == entry.id) entry else it } }
    }

    fun removeMemoryEntry(id: String) {
        _memoryEntries.update { it.filter { e -> e.id != id } }
    }

    fun clearMemory() {
        _memoryEntries.value = emptyList()
    }
}
