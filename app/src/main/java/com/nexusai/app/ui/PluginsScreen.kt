package com.nexusai.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexusai.core.ui.theme.NexusBackground
import com.nexusai.core.ui.theme.NexusCard
import com.nexusai.core.ui.theme.NexusPurple
import com.nexusai.core.ui.theme.NexusSurface
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary
import com.nexusai.domain.model.NexsusPlugin
import androidx.compose.ui.res.stringResource
import com.nexusai.app.R
import com.nexusai.domain.model.PluginCommand

data class PluginsUiState(
    val plugins: List<NexsusPlugin> = emptyList(),
    val selectedPlugin: NexsusPlugin? = null,
    val pluginCommands: List<PluginCommand> = emptyList(),
    val isExecuting: Boolean = false,
    val lastResult: String? = null,
    val showExecuteDialog: Boolean = false,
    val executeArgs: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginsScreen(
    onBack: () -> Unit = {},
    viewModel: PluginsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showExecuteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        TopAppBar(
            title = {
                Text(stringResource(R.string.plugins_title), color = NexusTextPrimary)
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBackground)
        )

        if (state.plugins.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = NexusTextTertiary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.plugins_empty),
                        style = MaterialTheme.typography.titleMedium,
                        color = NexusTextSecondary
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
                items(state.plugins, key = { it.id }) { plugin ->
                    PluginCard(
                        plugin = plugin,
                        onToggle = { viewModel.togglePlugin(plugin.id) },
                        onExecute = {
                            viewModel.selectPlugin(plugin)
                            showExecuteDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showExecuteDialog && state.selectedPlugin != null) {
        ExecutePluginDialog(
            pluginName = state.selectedPlugin!!.name,
            commands = state.pluginCommands,
            args = state.executeArgs,
            isExecuting = state.isExecuting,
            onArgsChange = { viewModel.setExecuteArgs(it) },
            onExecute = { commandId ->
                viewModel.executeCommand(commandId)
                showExecuteDialog = false
            },
            onDismiss = {
                showExecuteDialog = false
                viewModel.clearLastResult()
            }
        )
    }

    state.lastResult?.let { result ->
        AlertDialog(
            onDismissRequest = { viewModel.clearLastResult() },
            title = { Text(stringResource(R.string.plugins_result_title), color = NexusTextPrimary) },
            text = {
                Text(
                    text = result,
                    color = NexusTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearLastResult() }) {
                    Text("OK", color = NexusPurple)
                }
            },
            containerColor = NexusCard
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PluginCard(
    plugin: NexsusPlugin,
    onToggle: () -> Unit,
    onExecute: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (plugin.isEnabled) NexusCard else NexusSurface
        ),
        shape = RoundedCornerShape(12.dp)
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
                Text(
                    text = plugin.iconEmoji,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plugin.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = NexusTextPrimary
                    )
                    Text(
                        text = plugin.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusTextTertiary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                plugin.capabilities.forEach { cap ->
                    Text(
                        text = "${cap.emoji} ${cap.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusPurple,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NexusPurple.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (plugin.isBuiltIn) {
                    Text(
                        text = stringResource(R.string.plugins_built_in),
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusTextTertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(onClick = onExecute, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = stringResource(R.string.plugins_execute_title, ""),
                        tint = if (plugin.isEnabled) NexusPurple else NexusTextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (plugin.isEnabled) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = if (plugin.isEnabled) stringResource(R.string.plugins_enabled) else stringResource(R.string.plugins_disabled),
                        tint = if (plugin.isEnabled) NexusPurple else NexusTextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExecutePluginDialog(
    pluginName: String,
    commands: List<PluginCommand>,
    args: String,
    isExecuting: Boolean,
    onArgsChange: (String) -> Unit,
    onExecute: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.plugins_execute_title, pluginName), color = NexusTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = args,
                    onValueChange = onArgsChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.plugins_args_hint), color = NexusTextTertiary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NexusPurple,
                        unfocusedBorderColor = NexusSurface,
                        focusedContainerColor = NexusCard,
                        unfocusedContainerColor = NexusCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Text(
                    text = stringResource(R.string.plugins_commands),
                    style = MaterialTheme.typography.labelMedium,
                    color = NexusTextSecondary
                )

                commands.forEach { command ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExecute(command.id) },
                        colors = CardDefaults.cardColors(containerColor = NexusSurface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${command.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = NexusTextPrimary
                            )
                            Text(
                                text = command.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = NexusTextTertiary
                            )
                        }
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
