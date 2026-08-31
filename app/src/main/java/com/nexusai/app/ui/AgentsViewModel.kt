package com.nexusai.app.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AgentsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AgentsUiState())
    val uiState: StateFlow<AgentsUiState> = _uiState.asStateFlow()

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

        _uiState.value = state.copy(
            agents = state.agents + agent,
            newAgentName = "",
            newAgentDescription = "",
            newAgentPrompt = ""
        )
    }

    fun toggleAgent(id: String) {
        _uiState.value = _uiState.value.copy(
            agents = _uiState.value.agents.map {
                if (it.id == id) it.copy(isActive = !it.isActive) else it
            }
        )
    }

    fun deleteAgent(id: String) {
        _uiState.value = _uiState.value.copy(
            agents = _uiState.value.agents.filter { it.id != id }
        )
    }
}
