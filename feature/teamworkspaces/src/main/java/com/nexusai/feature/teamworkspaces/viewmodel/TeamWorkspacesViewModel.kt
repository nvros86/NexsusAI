package com.nexusai.feature.teamworkspaces.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.feature.teamworkspaces.MemberRole
import com.nexusai.feature.teamworkspaces.Workspace
import com.nexusai.feature.teamworkspaces.WorkspaceMember
import com.nexusai.feature.teamworkspaces.WorkspaceMessage
import com.nexusai.feature.teamworkspaces.WorkspaceService
import com.nexusai.domain.model.ActivityAction
import com.nexusai.domain.model.WorkspaceActivity
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
    val activities: List<WorkspaceActivity> = emptyList(),
    val isConnecting: Boolean = false,
    val showAddMemberDialog: Boolean = false,
    val error: String? = null
) {
    val onlineMemberCount: Int
        get() = members.count { it.isOnline }

    val totalMemberCount: Int
        get() = members.size
}

@HiltViewModel
class TeamWorkspacesViewModel @Inject constructor(
    private val workspaceService: WorkspaceService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamWorkspacesUiState())
    val uiState: StateFlow<TeamWorkspacesUiState> = _uiState.asStateFlow()

    private val _activities = MutableStateFlow<List<WorkspaceActivity>>(emptyList())

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
            loadDemoActivities(demoWorkspace.id)
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

    private fun loadDemoActivities(workspaceId: String) {
        val now = System.currentTimeMillis()
        val demoActivities = listOf(
            WorkspaceActivity(
                id = "act_1",
                workspaceId = workspaceId,
                memberId = "user_1",
                memberName = "Вы",
                action = ActivityAction.CREATED_WORKSPACE,
                targetName = "Демо workspace",
                timestamp = now - 600000
            ),
            WorkspaceActivity(
                id = "act_2",
                workspaceId = workspaceId,
                memberId = "user_1",
                memberName = "Вы",
                action = ActivityAction.ADDED_MEMBER,
                targetName = "Алиса",
                timestamp = now - 500000
            ),
            WorkspaceActivity(
                id = "act_3",
                workspaceId = workspaceId,
                memberId = "user_2",
                memberName = "Алиса",
                action = ActivityAction.SENT_MESSAGE,
                timestamp = now - 300000
            ),
            WorkspaceActivity(
                id = "act_4",
                workspaceId = workspaceId,
                memberId = "user_1",
                memberName = "Вы",
                action = ActivityAction.ADDED_MEMBER,
                targetName = "Боб",
                timestamp = now - 200000
            ),
            WorkspaceActivity(
                id = "act_5",
                workspaceId = workspaceId,
                memberId = "user_2",
                memberName = "Алиса",
                action = ActivityAction.CREATED_TAB,
                targetName = "Общий код",
                timestamp = now - 100000
            )
        )
        _activities.value = demoActivities
        _uiState.value = _uiState.value.copy(activities = demoActivities)
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
        addActivity(
            workspaceId = workspace.id,
            action = ActivityAction.CREATED_WORKSPACE,
            targetName = name
        )
    }

    fun selectWorkspace(workspace: Workspace) {
        _uiState.value = _uiState.value.copy(selectedWorkspace = workspace)
    }

    fun clearSelectedWorkspace() {
        _uiState.value = _uiState.value.copy(selectedWorkspace = null)
    }

    fun addMember(name: String, role: MemberRole) {
        val workspaceId = _uiState.value.selectedWorkspace?.id ?: return
        val newMember = WorkspaceMember(
            id = "user_${System.currentTimeMillis()}",
            name = name,
            role = role,
            isOnline = false
        )
        workspaceService.addMember(newMember)
        addActivity(
            workspaceId = workspaceId,
            action = ActivityAction.ADDED_MEMBER,
            targetName = name
        )
    }

    fun removeMember(memberId: String) {
        val workspaceId = _uiState.value.selectedWorkspace?.id ?: return
        val memberName = _uiState.value.members.find { it.id == memberId }?.name ?: ""
        workspaceService.removeMember(memberId)
        addActivity(
            workspaceId = workspaceId,
            action = ActivityAction.REMOVED_MEMBER,
            targetName = memberName
        )
    }

    fun changeMemberRole(memberId: String, newRole: MemberRole) {
        val workspaceId = _uiState.value.selectedWorkspace?.id ?: return
        val memberName = _uiState.value.members.find { it.id == memberId }?.name ?: ""
        val updatedMembers = _uiState.value.members.map {
            if (it.id == memberId) it.copy(role = newRole) else it
        }
        _uiState.value = _uiState.value.copy(members = updatedMembers)
        addActivity(
            workspaceId = workspaceId,
            action = ActivityAction.CHANGED_ROLE,
            targetName = "$memberName → ${newRole.displayName}"
        )
    }

    fun toggleMemberOnlineStatus(memberId: String) {
        val currentMember = _uiState.value.members.find { it.id == memberId } ?: return
        workspaceService.updateMemberStatus(memberId, !currentMember.isOnline)
    }

    fun showAddMemberDialog() {
        _uiState.value = _uiState.value.copy(showAddMemberDialog = true)
    }

    fun hideAddMemberDialog() {
        _uiState.value = _uiState.value.copy(showAddMemberDialog = false)
    }

    fun sendMessage(content: String) {
        val workspaceId = _uiState.value.selectedWorkspace?.id ?: return
        workspaceService.sendMessage(
            content = content,
            senderId = "user_1",
            senderName = "Вы"
        )
        addActivity(
            workspaceId = workspaceId,
            action = ActivityAction.SENT_MESSAGE
        )
    }

    private fun addActivity(workspaceId: String, action: ActivityAction, targetName: String = "") {
        val activity = WorkspaceActivity(
            id = "act_${System.currentTimeMillis()}",
            workspaceId = workspaceId,
            memberId = "user_1",
            memberName = "Вы",
            action = action,
            targetName = targetName
        )
        _activities.value = _activities.value + activity
        _uiState.value = _uiState.value.copy(activities = _activities.value)
    }

    fun refresh() {
        viewModelScope.launch {
            val demoWorkspace = workspaceService.createDemoWorkspace()
            _uiState.value = _uiState.value.copy(
                workspaces = listOf(demoWorkspace)
            )
            workspaceService.getDemoMessages()
            loadDemoActivities(demoWorkspace.id)
        }
    }
}
