package com.nexusai.feature.teamworkspaces.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.nexusai.feature.teamworkspaces.R
import com.nexusai.core.ui.theme.NexusBackground
import com.nexusai.core.ui.theme.NexusCard
import com.nexusai.core.ui.theme.NexusPurple
import com.nexusai.core.ui.theme.NexusSurface
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary
import com.nexusai.feature.teamworkspaces.MemberRole
import com.nexusai.feature.teamworkspaces.Workspace
import com.nexusai.feature.teamworkspaces.WorkspaceMember
import com.nexusai.feature.teamworkspaces.WorkspaceMessage
import com.nexusai.feature.teamworkspaces.viewmodel.TeamWorkspacesViewModel
import com.nexusai.domain.model.ActivityAction
import com.nexusai.domain.model.WorkspaceActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamWorkspacesScreen(
    onBack: () -> Unit = {},
    viewModel: TeamWorkspacesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showChat by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.workspaces_title), color = NexusTextPrimary)
                        Text(
                            text = stringResource(
                                R.string.workspaces_member_count_online,
                                state.totalMemberCount,
                                state.onlineMemberCount
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = NexusTextTertiary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_back_arrow),
                            tint = NexusTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.cd_refresh),
                            tint = NexusTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier.minimumInteractiveComponentSize(),
                containerColor = NexusPurple,
                contentColor = NexusTextPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_create_workspace)
                )
            }
        },
        containerColor = NexusBackground
    ) { padding ->
        if (state.workspaces.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = stringResource(R.string.cd_empty_state_icon),
                        modifier = Modifier.size(72.dp),
                        tint = NexusTextTertiary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.workspaces_empty),
                        style = MaterialTheme.typography.titleMedium,
                        color = NexusTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.workspaces_empty_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexusTextTertiary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
            ) {
                items(state.workspaces, key = { it.id }) { workspace ->
                    WorkspaceCard(
                        workspace = workspace,
                        onOpen = {
                            viewModel.selectWorkspace(workspace)
                            showChat = true
                        }
                    )
                }

                if (state.members.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.workspaces_all_members),
                                style = MaterialTheme.typography.titleMedium,
                                color = NexusTextPrimary
                            )
                            IconButton(
                                onClick = { viewModel.showAddMemberDialog() },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.workspaces_add_member),
                                    tint = NexusPurple
                                )
                            }
                        }
                    }

                    items(state.members, key = { it.id }) { member ->
                        MemberCard(
                            member = member,
                            onRemove = { viewModel.removeMember(member.id) },
                            onChangeRole = { newRole ->
                                viewModel.changeMemberRole(member.id, newRole)
                            },
                            onToggleOnline = { viewModel.toggleMemberOnlineStatus(member.id) }
                        )
                    }
                }

                if (state.activities.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.workspaces_activity_feed),
                            style = MaterialTheme.typography.titleMedium,
                            color = NexusTextPrimary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(state.activities.sortedByDescending { it.timestamp }, key = { it.id }) { activity ->
                        ActivityItem(activity = activity)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateWorkspaceDialog(
            onCreate = { name, description ->
                viewModel.createWorkspace(name, description)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    if (showChat) {
        state.selectedWorkspace?.let { workspace ->
            WorkspaceChatDialog(
                workspace = workspace,
                messages = state.messages,
                onSend = { content ->
                    viewModel.sendMessage(content)
                },
                onDismiss = {
                    showChat = false
                    viewModel.clearSelectedWorkspace()
                }
            )
        }
    }

    if (state.showAddMemberDialog) {
        AddMemberDialog(
            onAdd = { name, role ->
                viewModel.addMember(name, role)
                viewModel.hideAddMemberDialog()
            },
            onDismiss = { viewModel.hideAddMemberDialog() }
        )
    }
}

@Composable
private fun WorkspaceCard(
    workspace: Workspace,
    onOpen: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusCard),
        shape = RoundedCornerShape(12.dp),
        onClick = onOpen
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workspace.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = NexusTextPrimary
                    )
                    Text(
                        text = workspace.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusTextTertiary
                    )
                }

                Text(
                    text = stringResource(R.string.workspaces_members_count, workspace.members.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusPurple
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((-8).dp)
            ) {
                workspace.members.take(5).forEach { member ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (member.isOnline) NexusPurple else NexusTextTertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.name.first().toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = NexusTextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberCard(
    member: WorkspaceMember,
    onRemove: () -> Unit,
    onChangeRole: (MemberRole) -> Unit,
    onToggleOnline: () -> Unit
) {
    val onlineDesc = stringResource(R.string.cd_online_indicator)
    var showRoleMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = NexusCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (member.isOnline) NexusPurple else NexusTextTertiary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.first().toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = NexusTextPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = NexusTextPrimary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoleBadge(role = member.role)
                }
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (member.isOnline) {
                            NexusPurple
                        } else {
                            NexusTextTertiary
                        }
                    )
                    .semantics {
                        contentDescription = if (member.isOnline) onlineDesc else ""
                    }
            )

            if (member.id != "user_1") {
                IconButton(
                    onClick = { onRemove() },
                    modifier = Modifier
                        .size(32.dp)
                        .minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.workspaces_remove_member),
                        tint = NexusTextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleBadge(role: MemberRole) {
    val backgroundColor = when (role) {
        MemberRole.OWNER -> NexusPurple.copy(alpha = 0.2f)
        MemberRole.ADMIN -> NexusPurple.copy(alpha = 0.15f)
        MemberRole.EDITOR -> NexusPurple.copy(alpha = 0.1f)
        MemberRole.VIEWER -> NexusTextTertiary.copy(alpha = 0.1f)
    }
    val textColor = when (role) {
        MemberRole.OWNER -> NexusPurple
        MemberRole.ADMIN -> NexusPurple
        MemberRole.EDITOR -> NexusTextSecondary
        MemberRole.VIEWER -> NexusTextTertiary
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = role.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

@Composable
private fun ActivityItem(activity: WorkspaceActivity) {
    val actionText = when (activity.action) {
        ActivityAction.CREATED_WORKSPACE -> stringResource(R.string.workspaces_activity_created_workspace)
        ActivityAction.ADDED_MEMBER -> stringResource(R.string.workspaces_activity_added_member)
        ActivityAction.REMOVED_MEMBER -> stringResource(R.string.workspaces_activity_removed_member)
        ActivityAction.CHANGED_ROLE -> stringResource(R.string.workspaces_activity_changed_role)
        ActivityAction.CREATED_TAB -> stringResource(R.string.workspaces_activity_created_tab)
        ActivityAction.SENT_MESSAGE -> stringResource(R.string.workspaces_activity_sent_message)
        ActivityAction.UPLOADED_FILE -> stringResource(R.string.workspaces_activity_uploaded_file)
        ActivityAction.RAN_CHAIN -> stringResource(R.string.workspaces_activity_ran_chain)
    }

    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeText = dateFormat.format(Date(activity.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = NexusCard.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(NexusPurple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activity.memberName.first().toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusPurple
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${activity.memberName} $actionText",
                    style = MaterialTheme.typography.bodySmall,
                    color = NexusTextPrimary
                )
                if (activity.targetName.isNotEmpty()) {
                    Text(
                        text = activity.targetName,
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusTextTertiary
                    )
                }
            }

            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall,
                color = NexusTextTertiary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMemberDialog(
    onAdd: (String, MemberRole) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(MemberRole.VIEWER) }
    var roleExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.workspaces_add_member_title), color = NexusTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.workspaces_member_name_hint)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NexusPurple,
                        unfocusedBorderColor = NexusSurface,
                        focusedContainerColor = NexusCard,
                        unfocusedContainerColor = NexusCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.workspaces_member_email_hint)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NexusPurple,
                        unfocusedBorderColor = NexusSurface,
                        focusedContainerColor = NexusCard,
                        unfocusedContainerColor = NexusCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedRole.displayName,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        label = { Text(stringResource(R.string.workspaces_role_label)) },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexusPurple,
                            unfocusedBorderColor = NexusSurface,
                            focusedContainerColor = NexusCard,
                            unfocusedContainerColor = NexusCard
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        MemberRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(role.displayName, color = NexusTextPrimary)
                                        Text(
                                            when (role) {
                                                MemberRole.OWNER -> stringResource(R.string.workspaces_role_owner_desc)
                                                MemberRole.ADMIN -> stringResource(R.string.workspaces_role_admin_desc)
                                                MemberRole.EDITOR -> stringResource(R.string.workspaces_role_editor_desc)
                                                MemberRole.VIEWER -> stringResource(R.string.workspaces_role_viewer_desc)
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NexusTextTertiary
                                        )
                                    }
                                },
                                onClick = {
                                    selectedRole = role
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(name, selectedRole) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.workspaces_add_member_confirm), color = NexusPurple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.label_cancel), color = NexusTextSecondary)
            }
        },
        containerColor = NexusCard
    )
}

@Composable
private fun CreateWorkspaceDialog(
    onCreate: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.workspaces_new_title), color = NexusTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.chain_detail_name_label)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NexusPurple,
                        unfocusedBorderColor = NexusSurface,
                        focusedContainerColor = NexusCard,
                        unfocusedContainerColor = NexusCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.chain_detail_description_label)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NexusPurple,
                        unfocusedBorderColor = NexusSurface,
                        focusedContainerColor = NexusCard,
                        unfocusedContainerColor = NexusCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, description) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.agents_create_button), color = NexusPurple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.label_cancel), color = NexusTextSecondary)
            }
        },
        containerColor = NexusCard
    )
}

@Composable
private fun WorkspaceChatDialog(
    workspace: Workspace,
    messages: List<WorkspaceMessage>,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(workspace.name, color = NexusTextPrimary)
        },
        text = {
            Column {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        ChatMessageItem(message = message)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.workspaces_message_hint), color = NexusTextTertiary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexusPurple,
                            unfocusedBorderColor = NexusSurface,
                            focusedContainerColor = NexusCard,
                            unfocusedContainerColor = NexusCard
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            if (input.isNotBlank()) {
                                onSend(input)
                                input = ""
                            }
                        },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.cd_send_message),
                            tint = NexusPurple
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.prompts_close), color = NexusTextSecondary)
            }
        },
        containerColor = NexusCard
    )
}

@Composable
private fun ChatMessageItem(message: WorkspaceMessage) {
    val isSystem = message.type == com.nexusai.feature.teamworkspaces.MessageType.SYSTEM

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.senderId == "user_1") Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isSystem) NexusSurface else
                    if (message.senderId == "user_1") NexusPurple else NexusCard
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                if (!isSystem) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (message.senderId == "user_1") NexusTextPrimary.copy(alpha = 0.7f) else NexusPurple,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.senderId == "user_1") NexusTextPrimary else NexusTextSecondary
                )
            }
        }
    }
}
