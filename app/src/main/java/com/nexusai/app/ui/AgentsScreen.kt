package com.nexusai.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import com.nexusai.core.ui.theme.NexusBackground
import com.nexusai.core.ui.theme.NexusCard
import com.nexusai.core.ui.theme.NexusPurple
import com.nexusai.core.ui.theme.NexusSurface
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary
import com.nexusai.domain.model.AIAgent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentsScreen(
    onBack: () -> Unit = {},
    viewModel: AgentsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        TopAppBar(
            title = {
                Text("AI Агенты", color = NexusTextPrimary)
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBackground)
        )

        if (state.agents.isEmpty()) {
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
                        text = "Нет агентов",
                        style = MaterialTheme.typography.titleMedium,
                        color = NexusTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Создайте своего AI-агента",
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
                items(state.agents, key = { it.id }) { agent ->
                    AgentCard(
                        agent = agent,
                        onToggle = { viewModel.toggleAgent(agent.id) },
                        onDelete = { viewModel.deleteAgent(agent.id) }
                    )
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
                    contentDescription = "Создать агента"
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateAgentDialog(
            name = state.newAgentName,
            description = state.newAgentDescription,
            prompt = state.newAgentPrompt,
            onNameChange = { viewModel.setNewAgentName(it) },
            onDescriptionChange = { viewModel.setNewAgentDescription(it) },
            onPromptChange = { viewModel.setNewAgentPrompt(it) },
            onCreate = {
                viewModel.createAgent()
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
private fun AgentCard(
    agent: AIAgent,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (agent.isActive) NexusCard else NexusSurface
        ),
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
                    .background(NexusPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = NexusPurple
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = agent.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = NexusTextPrimary
                )
                Text(
                    text = agent.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = NexusTextTertiary,
                    maxLines = 1
                )
            }

            IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (agent.isActive) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (agent.isActive) "Активен" else "Отключен",
                    tint = if (agent.isActive) NexusPurple else NexusTextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = NexusTextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CreateAgentDialog(
    name: String,
    description: String,
    prompt: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPromptChange: (String) -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Новый агент", color = NexusTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Имя агента", color = NexusTextTertiary) },
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
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Описание", color = NexusTextTertiary) },
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
                    value = prompt,
                    onValueChange = onPromptChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Системный промпт", color = NexusTextTertiary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NexusPurple,
                        unfocusedBorderColor = NexusSurface,
                        focusedContainerColor = NexusCard,
                        unfocusedContainerColor = NexusCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onCreate,
                enabled = name.isNotBlank() && prompt.isNotBlank()
            ) {
                Text("Создать", color = NexusPurple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = NexusTextSecondary)
            }
        },
        containerColor = NexusCard
    )
}
