package com.nexusai.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexusai.core.ui.components.AIProviderIcon
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.ProviderType
import com.nexusai.feature.settings.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Provider")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "AI Providers",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(state.providers) { provider ->
                ProviderCard(
                    provider = provider,
                    onEdit = { viewModel.showEditDialog(provider) },
                    onDelete = { viewModel.deleteProvider(provider.id) },
                    onToggleFavorite = { viewModel.toggleFavorite(provider.id) }
                )
            }

            if (state.providers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = "No AI providers configured. Tap + to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (state.showAddDialog) {
        AddEditProviderDialog(
            provider = state.editingProvider,
            onDismiss = { viewModel.dismissDialog() },
            onSave = { name, type, apiKey, baseUrl, model, maxTokens, temperature ->
                viewModel.saveProvider(name, type, apiKey, baseUrl, model, maxTokens, temperature)
            }
        )
    }
}

@Composable
private fun ProviderCard(
    provider: AIProviderConfig,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AIProviderIcon(providerId = provider.type.name)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = provider.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = provider.type.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Model: ${provider.defaultModel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorite",
                    tint = if (provider.isFavorite) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit"
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddEditProviderDialog(
    provider: AIProviderConfig?,
    onDismiss: () -> Unit,
    onSave: (String, ProviderType, String, String, String, Int, Float) -> Unit
) {
    var name by remember { mutableStateOf(provider?.name ?: "") }
    var typeIndex by remember { mutableFloatStateOf(
        ProviderType.entries.indexOf(provider?.type ?: ProviderType.OPENAI).toFloat()
    ) }
    var apiKey by remember { mutableStateOf(provider?.apiKey ?: "") }
    var baseUrl by remember { mutableStateOf(provider?.baseUrl ?: "https://api.openai.com/v1") }
    var model by remember { mutableStateOf(provider?.defaultModel ?: "gpt-4o") }
    var maxTokens by remember { mutableFloatStateOf((provider?.maxTokens ?: 4096).toFloat()) }
    var temperature by remember { mutableFloatStateOf(provider?.temperature ?: 0.7f) }

    val selectedType = ProviderType.entries.getOrElse(typeIndex.toInt()) { ProviderType.OPENAI }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (provider != null) "Edit Provider" else "Add Provider")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Type: ${selectedType.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Slider(
                    value = typeIndex,
                    onValueChange = { typeIndex = it },
                    valueRange = 0f..(ProviderType.entries.size - 1).toFloat(),
                    steps = ProviderType.entries.size - 2
                )

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Default Model") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Max Tokens: ${maxTokens.toInt()}")
                Slider(
                    value = maxTokens,
                    onValueChange = { maxTokens = it },
                    valueRange = 256f..16384f,
                    steps = 15
                )

                Text("Temperature: %.1f".format(temperature))
                Slider(
                    value = temperature,
                    onValueChange = { temperature = it },
                    valueRange = 0f..2f,
                    steps = 19
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(name, selectedType, apiKey, baseUrl, model, maxTokens.toInt(), temperature)
                },
                enabled = name.isNotBlank() && apiKey.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
