package com.nexsusai.workstation.ui.workspace

import com.nexsusai.workstation.ai.AIModel

object AIModelSelector {
    val availableModels = listOf(
        AIModel("openai-gpt", "OpenAI GPT"),
        AIModel("anthropic-claude", "Anthropic Claude"),
        AIModel("local-llama", "Local Llama")
    )
}
