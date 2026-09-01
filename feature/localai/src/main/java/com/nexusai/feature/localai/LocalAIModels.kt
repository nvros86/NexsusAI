package com.nexusai.feature.localai

import kotlinx.serialization.Serializable

@Serializable
data class LocalAIConfig(
    val id: String,
    val name: String,
    val type: LocalAIType,
    val baseUrl: String = "http://localhost:11434",
    val isConnected: Boolean = false,
    val availableModels: List<LocalAIModel> = emptyList()
)

@Serializable
enum class LocalAIType(val displayName: String, val defaultPort: Int) {
    OLLAMA("Ollama", 11434),
    LLAMACPP("llama.cpp", 8080),
    LM_STUDIO("LM Studio", 1234),
    LOCALAI("LocalAI", 8080),
    CUSTOM("Custom", 8080)
}

@Serializable
data class LocalAIModel(
    val id: String,
    val name: String,
    val size: Long = 0,
    val parameterSize: String = "",
    val quantization: String = "",
    val isLoaded: Boolean = false,
    val vramUsage: Long = 0
)

data class LocalAIStatus(
    val isRunning: Boolean = false,
    val serverType: LocalAIType = LocalAIType.OLLAMA,
    val url: String = "",
    val modelsCount: Int = 0,
    val loadedModel: String? = null,
    val uptime: Long = 0,
    val error: String? = null
)
