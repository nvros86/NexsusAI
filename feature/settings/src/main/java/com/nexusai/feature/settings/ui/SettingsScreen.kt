package com.nexusai.feature.settings.ui

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexusai.core.ui.components.AIProviderIcon
import com.nexusai.core.ui.theme.NexusBackground
import com.nexusai.core.ui.theme.NexusCard
import com.nexusai.core.ui.theme.NexusPurple
import com.nexusai.core.ui.theme.NexusSurface
import com.nexusai.core.ui.theme.NexusSurfaceVariant
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary
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
                title = {
                    Text(
                        "Настройки модели",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NexusBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = NexusPurple,
                contentColor = NexusTextPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Provider")
            }
        },
        containerColor = NexusBackground
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
                    text = "Приватность и безопасность",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NexusTextPrimary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                PrivacyToggle(
                    title = "Режим инкогнито",
                    subtitle = "Не сохранять историю чатов",
                    icon = "🕶️",
                    checked = state.incognitoMode,
                    onCheckedChange = { viewModel.toggleIncognito() }
                )
            }

            item {
                PrivacyToggle(
                    title = "Тактильная отдача",
                    subtitle = "Вибрация при отправке и нажатиях",
                    icon = "📳",
                    checked = state.hapticFeedback,
                    onCheckedChange = { viewModel.toggleHaptic() }
                )
            }

            item {
                PrivacyToggle(
                    title = "Блокировка приложения",
                    subtitle = "Требовать PIN или биометрию",
                    icon = "🔒",
                    checked = state.appLock,
                    onCheckedChange = { viewModel.toggleAppLock() }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AI Провайдеры",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NexusTextPrimary,
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
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = NexusCard
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = NexusTextTertiary
                            )
                            Text(
                                text = "Нет настроенных AI провайдеров",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NexusTextSecondary
                            )
                            Text(
                                text = "Нажмите + чтобы добавить",
                                style = MaterialTheme.typography.bodySmall,
                                color = NexusTextTertiary
                            )
                        }
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
            containerColor = NexusCard
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(NexusPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    AIProviderIcon(providerId = provider.type.name)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = provider.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = NexusTextPrimary
                    )
                    Text(
                        text = provider.type.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusTextTertiary
                    )
                }

                Row {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorite",
                            tint = if (provider.isFavorite) NexusPurple else NexusTextTertiary
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = NexusTextSecondary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = NexusTextTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "API Key",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusTextTertiary
                    )
                    Text(
                        text = "••••••••${provider.apiKey.takeLast(4)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexusTextSecondary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Base URL",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusTextTertiary
                    )
                    Text(
                        text = provider.baseUrl,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexusTextSecondary,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Модель",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusTextTertiary
                    )
                    Text(
                        text = provider.defaultModel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexusTextSecondary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Max токены",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusTextTertiary
                    )
                    Text(
                        text = "${provider.maxTokens}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexusTextSecondary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Температура",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusTextTertiary
                    )
                    Text(
                        text = "${provider.temperature}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexusTextSecondary
                    )
                }
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
        containerColor = NexusSurface,
        title = {
            Text(
                text = if (provider != null) "Редактировать модель" else "Добавить модель",
                fontWeight = FontWeight.SemiBold,
                color = NexusTextPrimary
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = NexusSurfaceVariant,
                        focusedBorderColor = NexusPurple,
                        cursorColor = NexusPurple,
                        focusedTextColor = NexusTextPrimary,
                        unfocusedTextColor = NexusTextPrimary
                    )
                )

                Text(
                    text = "Провайдер: ${selectedType.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NexusTextSecondary
                )
                Slider(
                    value = typeIndex,
                    onValueChange = { typeIndex = it },
                    valueRange = 0f..(ProviderType.entries.size - 1).toFloat(),
                    steps = ProviderType.entries.size - 2,
                    colors = SliderDefaults.colors(
                        thumbColor = NexusPurple,
                        activeTrackColor = NexusPurple
                    )
                )

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = NexusSurfaceVariant,
                        focusedBorderColor = NexusPurple,
                        cursorColor = NexusPurple,
                        focusedTextColor = NexusTextPrimary,
                        unfocusedTextColor = NexusTextPrimary
                    )
                )

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = NexusSurfaceVariant,
                        focusedBorderColor = NexusPurple,
                        cursorColor = NexusPurple,
                        focusedTextColor = NexusTextPrimary,
                        unfocusedTextColor = NexusTextPrimary
                    )
                )

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Модель") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = NexusSurfaceVariant,
                        focusedBorderColor = NexusPurple,
                        cursorColor = NexusPurple,
                        focusedTextColor = NexusTextPrimary,
                        unfocusedTextColor = NexusTextPrimary
                    )
                )

                Column {
                    Text(
                        text = "Max токены: ${maxTokens.toInt()}",
                        color = NexusTextSecondary
                    )
                    Slider(
                        value = maxTokens,
                        onValueChange = { maxTokens = it },
                        valueRange = 256f..16384f,
                        steps = 15,
                        colors = SliderDefaults.colors(
                            thumbColor = NexusPurple,
                            activeTrackColor = NexusPurple
                        )
                    )
                }

                Column {
                    Text(
                        text = "Температура: %.1f".format(temperature),
                        color = NexusTextSecondary
                    )
                    Slider(
                        value = temperature,
                        onValueChange = { temperature = it },
                        valueRange = 0f..2f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = NexusPurple,
                            activeTrackColor = NexusPurple
                        )
                    )
                }

                Text(
                    text = "Системный промпт: Вы — полезный AI-ассистент NexusAI.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NexusTextTertiary
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
                Text("Сохранить", color = NexusPurple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = NexusTextTertiary)
            }
        }
    )
}

@Composable
private fun PrivacyToggle(
    title: String,
    subtitle: String,
    icon: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NexusCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = NexusTextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = NexusTextTertiary
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NexusTextPrimary,
                    checkedTrackColor = NexusPurple,
                    uncheckedThumbColor = NexusTextTertiary,
                    uncheckedTrackColor = NexusSurfaceVariant
                )
            )
        }
    }
}
