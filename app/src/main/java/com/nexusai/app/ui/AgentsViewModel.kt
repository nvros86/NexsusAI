package com.nexusai.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.data.common.AppDataManager
import com.nexusai.domain.model.AIAgent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgentsUiState(
    val agents: List<AIAgent> = emptyList(),
    val newAgentName: String = "",
    val newAgentDescription: String = "",
    val newAgentPrompt: String = "",
    val error: String? = null
)

@HiltViewModel
class AgentsViewModel @Inject constructor(
    private val appDataManager: AppDataManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentsUiState())
    val uiState: StateFlow<AgentsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appDataManager.agents.collect { agents ->
                _uiState.value = _uiState.value.copy(agents = agents)
            }
        }
    }

    fun setNewAgentName(name: String) {
        _uiState.value = _uiState.value.copy(newAgentName = name)
    }

    fun setNewAgentDescription(description: String) {
        _uiState.value = _uiState.value.copy(newAgentDescription = description)
    }

    fun setNewAgentPrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(newAgentPrompt = prompt)
    }

    fun createAgent() {
        val state = _uiState.value
        if (state.newAgentName.isBlank() || state.newAgentPrompt.isBlank()) return

        viewModelScope.launch {
            try {
                val agent = AIAgent(
                    id = System.currentTimeMillis().toString(),
                    name = state.newAgentName,
                    description = state.newAgentDescription,
                    systemPrompt = state.newAgentPrompt
                )
                appDataManager.addAgent(agent)
                _uiState.value = _uiState.value.copy(
                    newAgentName = "",
                    newAgentDescription = "",
                    newAgentPrompt = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Ошибка создания агента"
                )
            }
        }
    }

    fun toggleAgent(id: String) {
        val agent = _uiState.value.agents.find { it.id == id } ?: return
        viewModelScope.launch {
            try {
                appDataManager.updateAgent(agent.copy(isActive = !agent.isActive))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Ошибка обновления агента"
                )
            }
        }
    }

    fun deleteAgent(id: String) {
        viewModelScope.launch {
            try {
                appDataManager.removeAgent(id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Ошибка удаления агента"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
