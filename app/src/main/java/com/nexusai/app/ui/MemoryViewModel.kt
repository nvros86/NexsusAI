package com.nexusai.app.ui

import androidx.lifecycle.ViewModel
import com.nexusai.core.common.AppDataManager
import com.nexusai.domain.model.MemoryEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class MemoryUiState(
    val entries: List<MemoryEntry> = emptyList(),
    val searchQuery: String = "",
    val showAddDialog: Boolean = false,
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
        val currentEntries = appDataManager.memoryEntries.value
        _uiState.value = _uiState.value.copy(entries = currentEntries)
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

        val entry = MemoryEntry(
            id = System.currentTimeMillis().toString(),
            key = state.newKey,
            value = state.newValue
        )

        appDataManager.addMemoryEntry(entry)
        _uiState.value = state.copy(
            entries = appDataManager.memoryEntries.value,
            newKey = "",
            newValue = ""
        )
    }

    fun toggleImportant(id: String) {
        val entry = appDataManager.memoryEntries.value.find { it.id == id } ?: return
        appDataManager.updateMemoryEntry(entry.copy(isImportant = !entry.isImportant))
        _uiState.value = _uiState.value.copy(
            entries = appDataManager.memoryEntries.value
        )
    }

    fun deleteEntry(id: String) {
        appDataManager.removeMemoryEntry(id)
        _uiState.value = _uiState.value.copy(
            entries = appDataManager.memoryEntries.value
        )
    }

    fun clearAll() {
        appDataManager.clearMemory()
        _uiState.value = _uiState.value.copy(entries = emptyList())
    }
}
