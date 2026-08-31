package com.nexusai.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.ProviderType
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
    val editingProvider: AIProviderConfig? = null,
    val incognitoMode: Boolean = false,
    val hapticFeedback: Boolean = true,
    val appLock: Boolean = false
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
        type: ProviderType,
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
                aiProviderRepository.addProvider(
                    AIProviderConfig(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        type = type,
                        apiKey = apiKey,
                        baseUrl = baseUrl,
                        defaultModel = defaultModel,
                        maxTokens = maxTokens,
                        temperature = temperature
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

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            val provider = _state.value.providers.firstOrNull { it.id == id } ?: return@launch
            aiProviderRepository.updateProvider(provider.copy(isFavorite = !provider.isFavorite))
        }
    }

    fun toggleIncognito() {
        _state.value = _state.value.copy(incognitoMode = !_state.value.incognitoMode)
    }

    fun toggleHaptic() {
        _state.value = _state.value.copy(hapticFeedback = !_state.value.hapticFeedback)
    }

    fun toggleAppLock() {
        _state.value = _state.value.copy(appLock = !_state.value.appLock)
    }
}
