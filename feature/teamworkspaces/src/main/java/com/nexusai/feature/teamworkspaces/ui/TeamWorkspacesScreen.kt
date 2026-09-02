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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamWorkspacesScreen(
    onBack: () -> Unit = {},
    viewModel: TeamWorkspacesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showChat by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        TopAppBar(
            title = {
                Text(stringResource(R.string.workspaces_title), color = NexusTextPrimary)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.action_back),
                        tint = NexusTextPrimary
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.ai_router_refresh),
                        tint = NexusTextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBackground)
        )

        if (state.workspaces.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
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
                        Text(
                            text = stringResource(R.string.workspaces_online_members),
                            style = MaterialTheme.typography.titleMedium,
                            color = NexusTextPrimary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(state.members.filter { it.isOnline }, key = { it.id }) { member ->
                        MemberCard(member = member)
                    }
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier.padding(16.dp),
                containerColor = NexusPurple,
                contentColor = NexusTextPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.workspaces_create)
                )
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

    if (showChat && state.selectedWorkspace != null) {
        WorkspaceChatDialog(
            workspace = state.selectedWorkspace!!,
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
private fun MemberCard(member: WorkspaceMember) {
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
                Text(
                    text = member.role.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = NexusTextTertiary
                )
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (member.isOnline) NexusPurple else NexusTextTertiary)
            )
        }
    }
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

                    IconButton(onClick = {
                        if (input.isNotBlank()) {
                            onSend(input)
                            input = ""
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.workspaces_send),
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
