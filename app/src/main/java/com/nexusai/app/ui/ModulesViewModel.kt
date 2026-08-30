package com.nexusai.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.domain.model.NexusModule
import com.nexusai.domain.repository.ModuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModulesUiState(
    val modules: List<NexusModule> = emptyList(),
    val searchQuery: String = "",
    val selectedType: String? = null,
    val showEnabledOnly: Boolean = false
)

@HiltViewModel
class ModulesViewModel @Inject constructor(
    private val moduleRepository: ModuleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModulesUiState())
    val uiState: StateFlow<ModulesUiState> = _uiState.asStateFlow()

    init {
        loadModules()
    }

    private fun loadModules() {
        viewModelScope.launch {
            moduleRepository.getAllModules().collect { modules ->
                _uiState.value = _uiState.value.copy(modules = modules)
            }
        }
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        viewModelScope.launch {
            val flow = if (query.isEmpty()) {
                moduleRepository.getAllModules()
            } else {
                moduleRepository.searchModules(query)
            }
            flow.collect { modules ->
                _uiState.value = _uiState.value.copy(modules = modules)
            }
        }
    }

    fun selectType(type: String?) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        viewModelScope.launch {
            val flow = if (type == null) {
                moduleRepository.getAllModules()
            } else {
                moduleRepository.getModulesByType(type)
            }
            flow.collect { modules ->
                _uiState.value = _uiState.value.copy(modules = modules)
            }
        }
    }

    fun toggleModule(id: String, enabled: Boolean) {
        viewModelScope.launch {
            moduleRepository.setModuleEnabled(id, enabled)
        }
    }
}
