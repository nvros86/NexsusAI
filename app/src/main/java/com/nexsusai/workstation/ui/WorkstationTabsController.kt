package com.nexsusai.workstation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexsusai.workstation.data.SessionRepository
import com.nexsusai.workstation.data.local.WorkSessionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WorkstationTabsController @Inject constructor(
    private val repository: SessionRepository
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<WorkSessionEntity>>(emptyList())
    val sessions: StateFlow<List<WorkSessionEntity>> = _sessions.asStateFlow()

    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeSessions().collect { list ->
                _sessions.value = list
                if (_activeSessionId.value == null) {
                    _activeSessionId.value = list.firstOrNull()?.id
                }
            }
        }
    }

    fun createSession(name: String = "New AI Session") {
        viewModelScope.launch {
            val session = WorkSessionEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                provider = "unknown",
                context = ""
            )
            repository.saveSession(session)
            _activeSessionId.value = session.id
        }
    }

    fun selectSession(id: String) {
        _activeSessionId.value = id
    }

    fun closeSession(id: String) {
        viewModelScope.launch {
            repository.removeSession(id)
            if (_activeSessionId.value == id) {
                _activeSessionId.value = _sessions.value.firstOrNull { it.id != id }?.id
            }
        }
    }
}
