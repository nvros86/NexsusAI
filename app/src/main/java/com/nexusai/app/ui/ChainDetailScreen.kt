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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
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
import com.nexusai.domain.model.ChainStep
import com.nexusai.domain.model.ChainStepType

data class ChainDetailUiState(
    val chainName: String = "",
    val chainDescription: String = "",
    val steps: List<ChainStep> = emptyList(),
    val isRunning: Boolean = false,
    val lastResult: com.nexusai.domain.model.ChainRunResult? = null,
    val showAddStepDialog: Boolean = false,
    val newStepName: String = "",
    val newStepPrompt: String = "",
    val newStepType: ChainStepType = ChainStepType.TEXT_GENERATION
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChainDetailScreen(
    chainId: String,
    onBack: () -> Unit = {},
    viewModel: ChainDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showAddStepDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (chainId == "new") "Новая цепочка" else state.chainName,
                    color = NexusTextPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = NexusTextPrimary
                    )
                }
            },
            actions = {
                if (chainId != "new") {
                    IconButton(onClick = { viewModel.runChain() }) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Запустить",
                            tint = if (state.isRunning) NexusTextTertiary else NexusPurple
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBackground)
        )

        if (chainId == "new") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.chainName,
                    onValueChange = { viewModel.setChainName(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Название") },
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
                    value = state.chainDescription,
                    onValueChange = { viewModel.setChainDescription(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Описание") },
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
        } else {
            if (state.steps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Добавьте шаги",
                            style = MaterialTheme.typography.titleMedium,
                            color = NexusTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Нажмите + чтобы добавить шаг",
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
                    items(state.steps) { step ->
                        StepCard(
                            step = step,
                            onToggle = { viewModel.toggleStep(step.id) },
                            onDelete = { viewModel.deleteStep(step.id) }
                        )
                    }
                }
            }
        }

        if (state.lastResult != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.lastResult!!.isError) NexusTextTertiary.copy(alpha = 0.1f) else NexusPurple.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (state.lastResult!!.isError) "Ошибка" else "Результат",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = NexusTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    state.lastResult!!.stepResults.forEach { result ->
                        Text(
                            text = "${result.stepName}: ${result.output.take(100)}${if (result.output.length > 100) "..." else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NexusTextSecondary
                        )
                    }
                }
            }
        }

        if (chainId != "new") {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = { showAddStepDialog = true },
                    modifier = Modifier.padding(16.dp),
                    containerColor = NexusPurple,
                    contentColor = NexusTextPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить шаг"
                    )
                }
            }
        }
    }

    if (showAddStepDialog) {
        AddStepDialog(
            name = state.newStepName,
            prompt = state.newStepPrompt,
            stepType = state.newStepType,
            onNameChange = { viewModel.setNewStepName(it) },
            onPromptChange = { viewModel.setNewStepPrompt(it) },
            onTypeChange = { viewModel.setNewStepType(it) },
            onAdd = {
                viewModel.addStep()
                showAddStepDialog = false
            },
            onDismiss = { showAddStepDialog = false }
        )
    }
}

@Composable
private fun StepCard(
    step: ChainStep,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (step.isEnabled) NexusCard else NexusSurface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = step.type.emoji,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = step.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = NexusTextPrimary
                )
                Text(
                    text = step.prompt.take(80) + if (step.prompt.length > 80) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = NexusTextTertiary,
                    maxLines = 2
                )
            }

            IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (step.isEnabled) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (step.isEnabled) "Включён" else "Выключен",
                    tint = if (step.isEnabled) NexusPurple else NexusTextTertiary,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddStepDialog(
    name: String,
    prompt: String,
    stepType: ChainStepType,
    onNameChange: (String) -> Unit,
    onPromptChange: (String) -> Unit,
    onTypeChange: (ChainStepType) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Новый шаг", color = NexusTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = "${stepType.emoji} ${stepType.displayName}",
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        readOnly = true,
                        label = { Text("Тип") },
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
                        ChainStepType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text("${type.emoji} ${type.displayName}") },
                                onClick = {
                                    onTypeChange(type)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Имя шага", color = NexusTextTertiary) },
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
                    placeholder = { Text("Промпт (используйте {key} для подстановки)", color = NexusTextTertiary) },
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
                onClick = onAdd,
                enabled = name.isNotBlank() && prompt.isNotBlank()
            ) {
                Text("Добавить", color = NexusPurple)
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
