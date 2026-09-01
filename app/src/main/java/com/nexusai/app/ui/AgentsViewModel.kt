package com.nexusai.app.ui

import androidx.lifecycle.ViewModel
import com.nexusai.domain.common.AppDataManager
import com.nexusai.domain.model.AIAgent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class AgentsUiState(
    val agents: List<AIAgent> = emptyList(),
    val showCreateDialog: Boolean = false,
    val newAgentName: String = "",
    val newAgentDescription: String = "",
    val newAgentPrompt: String = ""
)

@HiltViewModel
class AgentsViewModel @Inject constructor(
    private val appDataManager: AppDataManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentsUiState())
    val uiState: StateFlow<AgentsUiState> = _uiState.asStateFlow()

    init {
        val currentAgents = appDataManager.agents.value
        _uiState.value = _uiState.value.copy(agents = currentAgents)
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

        val agent = AIAgent(
            id = System.currentTimeMillis().toString(),
            name = state.newAgentName,
            description = state.newAgentDescription,
            systemPrompt = state.newAgentPrompt
        )

        appDataManager.addAgent(agent)
        _uiState.value = state.copy(
            agents = appDataManager.agents.value,
            newAgentName = "",
            newAgentDescription = "",
            newAgentPrompt = ""
        )
    }

    fun toggleAgent(id: String) {
        val agent = appDataManager.agents.value.find { it.id == id } ?: return
        appDataManager.updateAgent(agent.copy(isActive = !agent.isActive))
        _uiState.value = _uiState.value.copy(
            agents = appDataManager.agents.value
        )
    }

    fun deleteAgent(id: String) {
        appDataManager.removeAgent(id)
        _uiState.value = _uiState.value.copy(
            agents = appDataManager.agents.value
        )
    }
}
