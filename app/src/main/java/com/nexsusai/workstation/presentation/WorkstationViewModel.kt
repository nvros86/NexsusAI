package com.nexsusai.workstation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexsusai.workstation.domain.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkstationViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkstationUiState())
    val uiState: StateFlow<WorkstationUiState> = _uiState.asStateFlow()

    init {
        observeSessions()
    }

    private fun observeSessions() {
        viewModelScope.launch {
            sessionManager.sessions.collect { sessions ->
                _uiState.value = _uiState.value.copy(
                    sessions = sessions
                )
            }
        }
    }

    fun createSession(name: String) {
        sessionManager.createSession(name)
    }

    fun selectSession(id: String) {
        _uiState.value = _uiState.value.copy(
            activeSessionId = id
        )
    }
}
