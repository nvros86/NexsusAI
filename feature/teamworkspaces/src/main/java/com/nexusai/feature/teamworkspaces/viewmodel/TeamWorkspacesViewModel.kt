package com.nexusai.feature.teamworkspaces.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.feature.teamworkspaces.MemberRole
import com.nexusai.feature.teamworkspaces.Workspace
import com.nexusai.feature.teamworkspaces.WorkspaceMember
import com.nexusai.feature.teamworkspaces.WorkspaceMessage
import com.nexusai.feature.teamworkspaces.WorkspaceService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamWorkspacesUiState(
    val workspaces: List<Workspace> = emptyList(),
    val selectedWorkspace: Workspace? = null,
    val members: List<WorkspaceMember> = emptyList(),
    val messages: List<WorkspaceMessage> = emptyList(),
    val isConnecting: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TeamWorkspacesViewModel @Inject constructor(
    private val workspaceService: WorkspaceService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamWorkspacesUiState())
    val uiState: StateFlow<TeamWorkspacesUiState> = _uiState.asStateFlow()

    init {
        loadWorkspaces()
    }

    private fun loadWorkspaces() {
        viewModelScope.launch {
            val demoWorkspace = workspaceService.createDemoWorkspace()
            _uiState.value = _uiState.value.copy(
                workspaces = listOf(demoWorkspace),
                members = workspaceService.members.value
            )
            workspaceService.getDemoMessages()
        }

        viewModelScope.launch {
            workspaceService.members.collect { members ->
                _uiState.value = _uiState.value.copy(members = members)
            }
        }

        viewModelScope.launch {
            workspaceService.messages.collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
    }

    fun createWorkspace(name: String, description: String) {
        val workspace = Workspace(
            id = System.currentTimeMillis().toString(),
            name = name,
            description = description,
            ownerId = "user_1",
            members = listOf(
                WorkspaceMember(
                    id = "user_1",
                    name = "Вы",
                    role = MemberRole.OWNER,
                    isOnline = true
                )
            )
        )
        _uiState.value = _uiState.value.copy(
            workspaces = _uiState.value.workspaces + workspace
        )
    }

    fun selectWorkspace(workspace: Workspace) {
        _uiState.value = _uiState.value.copy(selectedWorkspace = workspace)
    }

    fun clearSelectedWorkspace() {
        _uiState.value = _uiState.value.copy(selectedWorkspace = null)
    }

    fun sendMessage(content: String) {
        workspaceService.sendMessage(
            content = content,
            senderId = "user_1",
            senderName = "Вы"
        )
    }

    fun refresh() {
        viewModelScope.launch {
            val demoWorkspace = workspaceService.createDemoWorkspace()
            _uiState.value = _uiState.value.copy(
                workspaces = listOf(demoWorkspace)
            )
            workspaceService.getDemoMessages()
        }
    }
}
