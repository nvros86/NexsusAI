package com.nexusai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_providers")
data class AIProviderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val baseUrl: String,
    val apiKeyEncrypted: String,
    val modelsJson: String,
    val defaultModel: String,
    val maxTokens: Int,
    val temperature: Float,
    val systemPrompt: String,
    val customHeadersJson: String,
    val supportsImages: Boolean,
    val supportsFiles: Boolean,
    val supportsStreaming: Boolean,
    val isFavorite: Boolean
)
