package com.nexusai.domain.repository

interface AgentContextRepository {
    suspend fun getActiveAgentSystemPrompt(): String?
    suspend fun getMemoryContext(): String?
}
