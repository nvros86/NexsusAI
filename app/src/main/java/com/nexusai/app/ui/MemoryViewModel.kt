package com.nexusai.app.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MemoryViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setNewKey(key: String) {
        _uiState.value = _uiState.value.copy(newKey = key)
    }

    fun setNewValue(value: String) {
        _uiState.value = _uiState.value.copy(newValue = value)
    }

    fun addEntry() {
        val state = _uiState.value
        if (state.newKey.isBlank() || state.newValue.isBlank()) return

        val entry = MemoryEntry(
            id = System.currentTimeMillis().toString(),
            key = state.newKey,
            value = state.newValue
        )

        _uiState.value = state.copy(
            entries = state.entries + entry,
            newKey = "",
            newValue = ""
        )
    }

    fun toggleImportant(id: String) {
        _uiState.value = _uiState.value.copy(
            entries = _uiState.value.entries.map {
                if (it.id == id) it.copy(isImportant = !it.isImportant) else it
            }
        )
    }

    fun deleteEntry(id: String) {
        _uiState.value = _uiState.value.copy(
            entries = _uiState.value.entries.filter { it.id != id }
        )
    }

    fun clearAll() {
        _uiState.value = _uiState.value.copy(entries = emptyList())
    }
}
