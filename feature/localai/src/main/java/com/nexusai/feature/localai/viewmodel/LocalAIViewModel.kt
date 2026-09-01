package com.nexusai.feature.localai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.feature.localai.LocalAIConfig
import com.nexusai.feature.localai.LocalAIModel
import com.nexusai.feature.localai.LocalAIService
import com.nexusai.feature.localai.LocalAIStatus
import com.nexusai.feature.localai.LocalAIType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocalAIUiState(
    val configs: List<LocalAIConfig> = emptyList(),
    val models: List<LocalAIModel> = emptyList(),
    val status: LocalAIStatus = LocalAIStatus(),
    val selectedConfig: LocalAIConfig? = null,
    val isGenerating: Boolean = false,
    val lastResponse: String? = null
)

@HiltViewModel
class LocalAIViewModel @Inject constructor(
    private val localAIService: LocalAIService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocalAIUiState())
    val uiState: StateFlow<LocalAIUiState> = _uiState.asStateFlow()

    init {
        loadConfigs()
    }

    private fun loadConfigs() {
        viewModelScope.launch {
            val defaultConfigs = listOf(
                LocalAIConfig(
                    id = "ollama_default",
                    name = "Ollama",
                    type = LocalAIType.OLLAMA,
                    baseUrl = "http://localhost:11434"
                )
            )
            _uiState.value = _uiState.value.copy(configs = defaultConfigs)
            testConnection(defaultConfigs.first())
        }
    }

    fun addConfig(name: String, type: LocalAIType, url: String) {
        val config = LocalAIConfig(
            id = System.currentTimeMillis().toString(),
            name = name,
            type = type,
            baseUrl = url
        )
        _uiState.value = _uiState.value.copy(
            configs = _uiState.value.configs + config
        )
        testConnection(config)
    }

    fun deleteConfig(id: String) {
        _uiState.value = _uiState.value.copy(
            configs = _uiState.value.configs.filter { it.id != id }
        )
    }

    fun testConnection(config: LocalAIConfig) {
        viewModelScope.launch {
            val isConnected = localAIService.checkConnection(config.baseUrl)
            _uiState.value = _uiState.value.copy(
                configs = _uiState.value.configs.map {
                    if (it.id == config.id) it.copy(isConnected = isConnected) else it
                }
            )

            if (isConnected) {
                val models = localAIService.getModels(config.baseUrl)
                _uiState.value = _uiState.value.copy(
                    models = models,
                    status = LocalAIStatus(
                        isRunning = true,
                        serverType = config.type,
                        url = config.baseUrl,
                        modelsCount = models.size
                    )
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    status = LocalAIStatus(
                        isRunning = false,
                        error = "Не удалось подключиться"
                    )
                )
            }
        }
    }

    fun selectConfig(config: LocalAIConfig) {
        _uiState.value = _uiState.value.copy(selectedConfig = config)
    }

    fun sendPrompt(model: String, prompt: String) {
        val config = _uiState.value.selectedConfig ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true)

            try {
                val response = localAIService.generate(
                    baseUrl = config.baseUrl,
                    model = model,
                    prompt = prompt
                )
                _uiState.value = _uiState.value.copy(
                    lastResponse = response,
                    isGenerating = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    lastResponse = "Ошибка: ${e.message}",
                    isGenerating = false
                )
            }
        }
    }

    fun clearResponse() {
        _uiState.value = _uiState.value.copy(lastResponse = null)
    }

    fun pullModel(modelName: String) {
        val config = _uiState.value.configs.firstOrNull { it.isConnected } ?: return

        viewModelScope.launch {
            localAIService.pullModel(config.baseUrl, modelName)
            testConnection(config)
        }
    }

    fun deleteModel(modelName: String) {
        val config = _uiState.value.configs.firstOrNull { it.isConnected } ?: return

        viewModelScope.launch {
            localAIService.deleteModel(config.baseUrl, modelName)
            testConnection(config)
        }
    }

    fun refresh() {
        _uiState.value.configs.forEach { config ->
            testConnection(config)
        }
    }
}
