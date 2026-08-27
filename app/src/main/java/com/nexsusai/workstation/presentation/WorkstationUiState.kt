package com.nexsusai.workstation.presentation

import com.nexsusai.workstation.domain.WorkSession

data class WorkstationUiState(
    val sessions: List<WorkSession> = emptyList(),
    val activeSessionId: String? = null
)
