package com.nexusai.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.app.R
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
    val newValue: String = "",
    val error: String? = null
)

@HiltViewModel
class MemoryViewModel @Inject constructor(
    application: Application,
    private val appDataManager: AppDataManager
) : AndroidViewModel(application) {

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
            try {
                val entry = MemoryEntry(
                    id = System.currentTimeMillis().toString(),
                    key = state.newKey,
                    value = state.newValue
                )
                appDataManager.addMemoryEntry(entry)
                _uiState.value = _uiState.value.copy(newKey = "", newValue = "")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = getApplication<Application>().getString(R.string.error_memory_add, e.message ?: "")
                )
            }
        }
    }

    fun toggleImportant(id: String) {
        val entry = _uiState.value.entries.find { it.id == id } ?: return
        viewModelScope.launch {
            try {
                appDataManager.updateMemoryEntry(entry.copy(isImportant = !entry.isImportant))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = getApplication<Application>().getString(R.string.error_memory_update, e.message ?: "")
                )
            }
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            try {
                appDataManager.removeMemoryEntry(id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = getApplication<Application>().getString(R.string.error_memory_delete, e.message ?: "")
                )
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            try {
                appDataManager.clearMemory()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = getApplication<Application>().getString(R.string.error_memory_clear, e.message ?: "")
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
