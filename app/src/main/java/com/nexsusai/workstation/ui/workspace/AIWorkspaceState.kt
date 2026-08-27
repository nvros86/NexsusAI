package com.nexsusai.workstation.ui.workspace

import com.nexsusai.workstation.ai.AIModel

data class AIWorkspaceState(
    val selectedModel: AIModel? = null,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isGenerating: Boolean = false
)
