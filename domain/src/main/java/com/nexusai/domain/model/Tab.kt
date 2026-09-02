package com.nexusai.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Tab(
    val id: String,
    val title: String,
    val aiProviderId: String? = null,
    val agentId: String? = null,
    val messages: List<Message> = emptyList(),
    val attachedFiles: List<AttachedFile> = emptyList(),
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val accentColor: Long = 0xFF6750A4
)

@Serializable
data class Message(
    val id: String,
    val content: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis(),
    val attachments: List<AttachedFile> = emptyList(),
    val isStreaming: Boolean = false
)

@Serializable
data class AttachedFile(
    val id: String,
    val name: String,
    val uri: String,
    val mimeType: String,
    val size: Long
)
