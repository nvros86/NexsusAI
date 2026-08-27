package com.nexsusai.workstation.ui.workspace

/** Message model for AI Workspace chat UI. */
data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
