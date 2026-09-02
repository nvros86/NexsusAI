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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.nexusai.core.ui.theme.NexusSurfaceVariant
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary
import com.nexusai.domain.model.ComparisonMode
import com.nexusai.domain.model.SplitResult

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SplitViewScreen(
    onBack: () -> Unit = {},
    viewModel: SplitViewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        TopAppBar(
            title = { Text("Split View", color = NexusTextPrimary) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = NexusTextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBackground)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Режим сравнения",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = NexusTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ComparisonMode.entries.forEach { mode ->
                        ModeChip(
                            label = mode.displayName,
                            selected = uiState.comparisonMode == mode,
                            onClick = { viewModel.setComparisonMode(mode) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Провайдеры",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = NexusTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.availableProviders.forEach { provider ->
                        val isSelected = uiState.selectedProviders.any { it.id == provider.id }
                        val isFull = uiState.selectedProviders.size >= uiState.comparisonMode.count
                        ProviderChip(
                            name = provider.name,
                            selected = isSelected,
                            enabled = isSelected || !isFull,
                            onClick = { viewModel.toggleProvider(provider) }
                        )
                    }
                }
                if (uiState.availableProviders.isEmpty()) {
                    Text(
                        text = "Нет провайдеров с API-ключами. Добавьте в настройках.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusTextTertiary
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = { viewModel.setQuery(it) },
                    placeholder = { Text("Задайте вопрос для сравнения...", color = NexusTextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = NexusSurfaceVariant,
                        focusedBorderColor = NexusPurple,
                        cursorColor = NexusPurple,
                        focusedTextColor = NexusTextPrimary,
                        unfocusedTextColor = NexusTextPrimary
                    ),
                    minLines = 2,
                    maxLines = 4
                )
            }

            item {
                IconButton(
                    onClick = { viewModel.runComparison() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.query.isNotBlank() && uiState.selectedProviders.isNotEmpty() && !uiState.isRunning
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (uiState.isRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = NexusPurple,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Сравнение...", color = NexusPurple)
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = NexusPurple
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Сравнить ответы",
                                color = NexusPurple,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (uiState.results.isNotEmpty()) {
                item {
                    Text(
                        text = "Результаты",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = NexusTextSecondary
                    )
                }

                items(uiState.results, key = { it.providerId }) { result ->
                    ResultCard(
                        result = result,
                        onRate = { rating -> viewModel.rateResult(result.providerId, rating) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) NexusPurple else NexusSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = if (selected) NexusTextPrimary else NexusTextSecondary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ProviderChip(name: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                when {
                    selected -> NexusPurple
                    !enabled -> NexusSurfaceVariant.copy(alpha = 0.5f)
                    else -> NexusSurface
                }
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = NexusTextPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = name,
                color = if (selected) NexusTextPrimary else NexusTextSecondary,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ResultCard(result: SplitResult, onRate: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                result.error != null -> NexusSurfaceVariant.copy(alpha = 0.5f)
                result.isLoading -> NexusCard
                else -> NexusCard
            }
        )
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
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NexusPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = NexusPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.providerName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = NexusTextPrimary
                    )
                    Text(
                        text = result.modelName,
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusTextTertiary
                    )
                }

                if (!result.isLoading && result.error == null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${result.latencyMs}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = NexusTextTertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NexusPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (result.error != null) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                result.isLoading -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = NexusPurple,
                        trackColor = NexusSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Генерация ответа...",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusTextTertiary
                    )
                }
                result.error != null -> {
                    Text(
                        text = "Ошибка: ${result.error}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    Text(
                        text = result.response,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexusTextPrimary
                    )

                    if (result.tokensUsed > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "~${result.tokensUsed} токенов",
                            style = MaterialTheme.typography.labelSmall,
                            color = NexusTextTertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Оценка:",
                            style = MaterialTheme.typography.labelSmall,
                            color = NexusTextTertiary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = { onRate(star) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (star <= result.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$star",
                                    tint = if (star <= result.rating) NexusPurple else NexusTextTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
