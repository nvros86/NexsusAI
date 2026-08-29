package com.nexusai.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.domain.model.Prompt
import com.nexusai.domain.repository.PromptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PromptsUiState(
    val prompts: List<Prompt> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val showFavoritesOnly: Boolean = false,
    val copiedPromptId: String? = null
)

@HiltViewModel
class PromptsViewModel @Inject constructor(
    private val promptRepository: PromptRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PromptsUiState())
    val uiState: StateFlow<PromptsUiState> = _uiState.asStateFlow()

    init {
        loadPrompts()
    }

    private fun loadPrompts() {
        viewModelScope.launch {
            promptRepository.getAllPrompts().collect { prompts ->
                _uiState.value = _uiState.value.copy(prompts = prompts)
            }
        }
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        viewModelScope.launch {
            val flow = if (query.isEmpty()) {
                if (_uiState.value.showFavoritesOnly) {
                    promptRepository.getFavoritePrompts()
                } else {
                    promptRepository.getAllPrompts()
                }
            } else {
                promptRepository.searchPrompts(query)
            }
            flow.collect { prompts ->
                _uiState.value = _uiState.value.copy(prompts = prompts)
            }
        }
    }

    fun selectCategory(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category, showFavoritesOnly = false)
        viewModelScope.launch {
            val flow = if (category == null) {
                promptRepository.getAllPrompts()
            } else {
                promptRepository.getPromptsByCategory(category)
            }
            flow.collect { prompts ->
                _uiState.value = _uiState.value.copy(prompts = prompts)
            }
        }
    }

    fun toggleFavoritesOnly() {
        val newValue = !_uiState.value.showFavoritesOnly
        _uiState.value = _uiState.value.copy(showFavoritesOnly = newValue, selectedCategory = null)
        viewModelScope.launch {
            val flow = if (newValue) {
                promptRepository.getFavoritePrompts()
            } else {
                promptRepository.getAllPrompts()
            }
            flow.collect { prompts ->
                _uiState.value = _uiState.value.copy(prompts = prompts)
            }
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            promptRepository.toggleFavorite(id)
        }
    }

    fun copyPrompt(id: String) {
        viewModelScope.launch {
            promptRepository.incrementUsage(id)
            _uiState.value = _uiState.value.copy(copiedPromptId = id)
        }
    }

    fun dismissCopied() {
        _uiState.value = _uiState.value.copy(copiedPromptId = null)
    }
}
