package com.nexusai.feature.localai.ui

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexusai.feature.localai.R
import com.nexusai.core.ui.theme.NexusBackground
import com.nexusai.core.ui.theme.NexusCard
import com.nexusai.core.ui.theme.NexusPurple
import com.nexusai.core.ui.theme.NexusSurface
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary
import com.nexusai.feature.localai.LocalAIConfig
import com.nexusai.feature.localai.LocalAIModel
import com.nexusai.feature.localai.LocalAIStatus
import com.nexusai.feature.localai.LocalAIType
import com.nexusai.feature.localai.viewmodel.LocalAIViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalAIScreen(
    onBack: () -> Unit = {},
    viewModel: LocalAIViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        TopAppBar(
            title = {
                Text("Local AI", color = NexusTextPrimary)
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
                    contentDescription = stringResource(R.string.localai_refresh),
                    tint = NexusTextPrimary
                )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBackground)
        )

        StatusCard(status = state.status)

        Spacer(modifier = Modifier.height(16.dp))

        if (state.configs.isEmpty()) {
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
                        text = stringResource(R.string.localai_no_connections),
                        style = MaterialTheme.typography.titleMedium,
                        color = NexusTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.localai_add_server_hint),
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
                items(state.configs, key = { it.id }) { config ->
                    ServerCard(
                        config = config,
                        onTest = { viewModel.testConnection(config) },
                        onChat = {
                            viewModel.selectConfig(config)
                            showChatDialog = true
                        },
                        onDelete = { viewModel.deleteConfig(config.id) }
                    )
                }

                if (state.models.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.localai_available_models),
                            style = MaterialTheme.typography.titleMedium,
                            color = NexusTextPrimary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(state.models, key = { it.id }) { model ->
                        ModelCard(
                            model = model,
                            onPull = { viewModel.pullModel(model.name) },
                            onDelete = { viewModel.deleteModel(model.name) }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.padding(16.dp),
                containerColor = NexusPurple,
                contentColor = NexusTextPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.localai_add_server)
                )
            }
        }
    }

    if (showAddDialog) {
        AddServerDialog(
            configs = state.configs,
            onAdd = { name, type, url ->
                viewModel.addConfig(name, type, url)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    if (showChatDialog) {
        state.selectedConfig?.let { config ->
            LocalAIChatDialog(
                config = config,
                models = state.models,
                onSend = { model, prompt ->
                    viewModel.sendPrompt(model, prompt)
                },
                response = state.lastResponse,
                isGenerating = state.isGenerating,
                onDismiss = {
                    showChatDialog = false
                    viewModel.clearResponse()
                }
            )
        }
    }
}

@Composable
private fun StatusCard(status: LocalAIStatus) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (status.isRunning) NexusPurple.copy(alpha = 0.1f) else NexusCard
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (status.isRunning) NexusPurple else NexusTextTertiary)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (status.isRunning) stringResource(R.string.localai_server_running) else stringResource(R.string.localai_server_stopped),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = NexusTextPrimary
                )
                if (status.isRunning) {
                    Text(
                        text = stringResource(R.string.localai_models_format, status.modelsCount, status.loadedModel ?: stringResource(R.string.localai_no_loaded_model)),
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusTextTertiary
                    )
                }
            }

            status.error?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = NexusTextTertiary
                )
            }
        }
    }
}

@Composable
private fun ServerCard(
    config: LocalAIConfig,
    onTest: () -> Unit,
    onChat: () -> Unit,
    onDelete: () -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = config.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = NexusTextPrimary
                )
                Text(
                    text = stringResource(R.string.localai_server_name, config.type.displayName, config.baseUrl),
                    style = MaterialTheme.typography.bodySmall,
                    color = NexusTextTertiary
                )
            }

            IconButton(onClick = onTest, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.localai_test),
                    tint = NexusPurple,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onChat, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.localai_chat_action),
                    tint = if (config.isConnected) NexusPurple else NexusTextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.label_delete),
                    tint = NexusTextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ModelCard(
    model: LocalAIModel,
    onPull: () -> Unit,
    onDelete: () -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = NexusTextPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (model.parameterSize.isNotEmpty()) {
                        Text(
                            text = model.parameterSize,
                            style = MaterialTheme.typography.labelSmall,
                            color = NexusPurple
                        )
                    }
                    if (model.quantization.isNotEmpty()) {
                        Text(
                            text = model.quantization,
                            style = MaterialTheme.typography.labelSmall,
                            color = NexusTextTertiary
                        )
                    }
                    if (model.size > 0) {
                        Text(
                            text = formatSize(model.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = NexusTextTertiary
                        )
                    }
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.label_delete),
                    tint = NexusTextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddServerDialog(
    configs: List<LocalAIConfig>,
    onAdd: (String, LocalAIType, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(LocalAIType.OLLAMA) }
    var url by remember { mutableStateOf("http://localhost:11434") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.localai_add_server), color = NexusTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.localai_name_label)) },
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
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = type.displayName,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        readOnly = true,
                        label = { Text(stringResource(R.string.localai_server_type)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexusPurple,
                            unfocusedBorderColor = NexusSurface,
                            focusedContainerColor = NexusCard,
                            unfocusedContainerColor = NexusCard
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        LocalAIType.entries.forEach { serverType ->
                            DropdownMenuItem(
                                text = { Text("${serverType.displayName} (:${serverType.defaultPort})") },
                                onClick = {
                                    type = serverType
                                    url = "http://localhost:${serverType.defaultPort}"
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.localai_url_label)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NexusPurple,
                        unfocusedBorderColor = NexusSurface,
                        focusedContainerColor = NexusCard,
                        unfocusedContainerColor = NexusCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(name, type, url) },
                enabled = name.isNotBlank() && url.isNotBlank()
            ) {
                Text(stringResource(R.string.localai_add), color = NexusPurple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close), color = NexusTextSecondary)
            }
        },
        containerColor = NexusCard
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalAIChatDialog(
    config: LocalAIConfig,
    models: List<LocalAIModel>,
    onSend: (String, String) -> Unit,
    response: String?,
    isGenerating: Boolean,
    onDismiss: () -> Unit
) {
    var selectedModel by remember { mutableStateOf(models.firstOrNull()?.name ?: "") }
    var prompt by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.localai_chat, config.name), color = NexusTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (models.isNotEmpty()) {
                    var modelExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = modelExpanded,
                        onExpandedChange = { modelExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedModel,
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            readOnly = true,
                            label = { Text(stringResource(R.string.localai_model_label)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NexusPurple,
                                unfocusedBorderColor = NexusSurface,
                                focusedContainerColor = NexusCard,
                                unfocusedContainerColor = NexusCard
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = modelExpanded,
                            onDismissRequest = { modelExpanded = false }
                        ) {
                            models.forEach { model ->
                                DropdownMenuItem(
                                    text = { Text(model.name) },
                                    onClick = {
                                        selectedModel = model.name
                                        modelExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.localai_prompt)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NexusPurple,
                        unfocusedBorderColor = NexusSurface,
                        focusedContainerColor = NexusCard,
                        unfocusedContainerColor = NexusCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )

                if (isGenerating) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = NexusPurple,
                            strokeWidth = 2.dp
                        )
                    }
                }

                if (response != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NexusSurface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = response,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = NexusTextSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSend(selectedModel, prompt) },
                enabled = selectedModel.isNotBlank() && prompt.isNotBlank() && !isGenerating
            ) {
                Text(stringResource(R.string.localai_send), color = NexusPurple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.localai_close), color = NexusTextSecondary)
            }
        },
        containerColor = NexusCard
    )
}

private fun formatSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1 -> String.format("%.1f GB", gb)
        mb >= 1 -> String.format("%.1f MB", mb)
        else -> String.format("%.1f KB", kb)
    }
}
