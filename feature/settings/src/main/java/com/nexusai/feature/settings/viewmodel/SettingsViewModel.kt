package com.nexusai.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.repository.AIProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SettingsState(
    val providers: List<AIProviderConfig> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val editingProvider: AIProviderConfig? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val aiProviderRepository: AIProviderRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            aiProviderRepository.getAllProviders().collect { providers ->
                _state.value = _state.value.copy(providers = providers)
            }
        }
    }

    fun showAddDialog() {
        _state.value = _state.value.copy(
            showAddDialog = true,
            editingProvider = null
        )
    }

    fun showEditDialog(provider: AIProviderConfig) {
        _state.value = _state.value.copy(
            showAddDialog = true,
            editingProvider = provider
        )
    }

    fun dismissDialog() {
        _state.value = _state.value.copy(
            showAddDialog = false,
            editingProvider = null
        )
    }

    fun saveProvider(
        name: String,
        type: String,
        apiKey: String,
        baseUrl: String,
        defaultModel: String,
        maxTokens: Int,
        temperature: Float
    ) {
        viewModelScope.launch {
            val editing = _state.value.editingProvider
            if (editing != null) {
                aiProviderRepository.updateProvider(
                    editing.copy(
                        name = name,
                        type = type,
                        apiKey = apiKey,
                        baseUrl = baseUrl,
                        defaultModel = defaultModel,
                        maxTokens = maxTokens,
                        temperature = temperature
                    )
                )
            } else {
                aiProviderRepository.createProvider(
                    AIProviderConfig(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        type = type,
                        apiKey = apiKey,
                        baseUrl = baseUrl,
                        defaultModel = defaultModel,
                        maxTokens = maxTokens,
                        temperature = temperature,
                        isActive = true
                    )
                )
            }
            dismissDialog()
        }
    }

    fun deleteProvider(id: String) {
        viewModelScope.launch {
            aiProviderRepository.deleteProvider(id)
        }
    }

    fun toggleProviderActive(id: String, isActive: Boolean) {
        viewModelScope.launch {
            val provider = _state.value.providers.firstOrNull { it.id == id } ?: return@launch
            aiProviderRepository.updateProvider(provider.copy(isActive = isActive))
        }
    }
}
