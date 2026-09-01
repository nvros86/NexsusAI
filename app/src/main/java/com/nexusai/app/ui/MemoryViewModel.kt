package com.nexusai.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.data.common.AppDataManager
import com.nexusai.domain.model.MemoryEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemoryUiState(
    val entries: List<MemoryEntry> = emptyList(),
    val searchQuery: String = "",
    val newKey: String = "",
    val newValue: String = ""
)

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val appDataManager: AppDataManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appDataManager.memoryEntries.collect { entries ->
                _uiState.value = _uiState.value.copy(entries = entries)
            }
        }
    }

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

        viewModelScope.launch {
            val entry = MemoryEntry(
                id = System.currentTimeMillis().toString(),
                key = state.newKey,
                value = state.newValue
            )
            appDataManager.addMemoryEntry(entry)
            _uiState.value = _uiState.value.copy(newKey = "", newValue = "")
        }
    }

    fun toggleImportant(id: String) {
        val entry = _uiState.value.entries.find { it.id == id } ?: return
        viewModelScope.launch {
            appDataManager.updateMemoryEntry(entry.copy(isImportant = !entry.isImportant))
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            appDataManager.removeMemoryEntry(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            appDataManager.clearMemory()
        }
    }
}
