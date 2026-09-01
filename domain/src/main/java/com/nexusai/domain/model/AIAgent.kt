package com.nexusai.domain.model

data class AIAgent(
    val id: String,
    val name: String,
    val description: String,
    val systemPrompt: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class MemoryEntry(
    val id: String,
    val key: String,
    val value: String,
    val isImportant: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
