package com.nexusai.feature.aiprovider.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.repository.AIProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AIProviderUiState(
    val provider: AIProviderConfig? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AIProviderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val providerRepository: AIProviderRepository
) : ViewModel() {

    private val providerId: String = savedStateHandle["providerId"] ?: ""

    private val _state = MutableStateFlow(AIProviderUiState())
    val state: StateFlow<AIProviderUiState> = _state.asStateFlow()

    init {
        loadProvider()
    }

    private fun loadProvider() {
        if (providerId.isEmpty()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val provider = providerRepository.getProviderById(providerId)
                _state.value = _state.value.copy(
                    provider = provider,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
}
