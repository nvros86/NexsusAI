package com.nexusai.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.MarketplaceProvider
import com.nexusai.domain.repository.AIProviderRepository
import com.nexusai.domain.repository.MarketplaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class MarketplaceUiState(
    val providers: List<MarketplaceProvider> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val isSearching: Boolean = false,
    val addedProviderName: String? = null,
    val error: String? = null
)

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository,
    private val aiProviderRepository: AIProviderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketplaceUiState())
    val uiState: StateFlow<MarketplaceUiState> = _uiState.asStateFlow()

    init {
        loadProviders()
    }

    private fun loadProviders() {
        viewModelScope.launch {
            marketplaceRepository.getAllPresets().collect { presets ->
                _uiState.value = _uiState.value.copy(providers = presets)
            }
        }
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, isSearching = query.isNotEmpty())
        viewModelScope.launch {
            val flow = if (query.isEmpty()) {
                marketplaceRepository.getAllPresets()
            } else {
                marketplaceRepository.searchPresets(query)
            }
            flow.collect { presets ->
                _uiState.value = _uiState.value.copy(providers = presets)
            }
        }
    }

    fun selectCategory(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        viewModelScope.launch {
            val flow = if (category == null) {
                marketplaceRepository.getAllPresets()
            } else {
                marketplaceRepository.getPresetsByCategory(category)
            }
            flow.collect { presets ->
                _uiState.value = _uiState.value.copy(providers = presets)
            }
        }
    }

    fun addProvider(preset: MarketplaceProvider, apiKey: String = "") {
        viewModelScope.launch {
            try {
                val provider = AIProviderConfig(
                    id = UUID.randomUUID().toString(),
                    name = preset.name,
                    type = preset.type,
                    baseUrl = preset.baseUrl,
                    apiKey = apiKey,
                    models = preset.models,
                    defaultModel = preset.defaultModel,
                    maxTokens = preset.maxTokens,
                    temperature = preset.temperature,
                    supportsImages = preset.capabilities.contains(
                        com.nexusai.domain.model.ProviderCapability.VISION
                    ),
                    supportsStreaming = preset.capabilities.contains(
                        com.nexusai.domain.model.ProviderCapability.STREAMING
                    )
                )
                aiProviderRepository.addProvider(provider)
                marketplaceRepository.markAsAdded(preset.id)
                _uiState.value = _uiState.value.copy(addedProviderName = preset.name)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка добавления провайдера: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun dismissAddedMessage() {
        _uiState.value = _uiState.value.copy(addedProviderName = null)
    }
}
