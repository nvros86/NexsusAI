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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import com.nexusai.core.ui.theme.NexusSurfaceVariant
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary
import androidx.compose.ui.res.stringResource
import com.nexusai.app.R
import com.nexusai.domain.model.RoutingStrategy

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AIRouterScreen(
    onBack: () -> Unit = {},
    viewModel: AIRouterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTestDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.ai_router_title), color = NexusTextPrimary) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = NexusTextPrimary
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.runRouting() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.ai_router_refresh),
                        tint = NexusPurple
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBackground)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.ai_router_strategy),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NexusTextPrimary
                )
            }

            items(RoutingStrategy.entries, key = { it.name }) { strategy ->
                StrategyCard(
                    strategy = strategy,
                    selected = uiState.strategy == strategy,
                    onClick = { viewModel.setStrategy(strategy) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.ai_router_providers),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NexusTextPrimary
                )
            }

            items(uiState.providers, key = { it.id }) { provider ->
                ProviderStatusCard(provider = provider)
            }

            uiState.routingResult?.let { result ->
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.ai_router_result),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = NexusTextPrimary
                    )
                }

                item {
                    RoutingResultCard(result = result)
                }

                if (result.failoverChain.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.ai_router_failover_chain),
                            style = MaterialTheme.typography.titleSmall,
                            color = NexusTextSecondary
                        )
                    }

                    items(result.failoverChain, key = { it.id }) { provider ->
                        ProviderStatusCard(provider = provider)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = { showTestDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.ai_router_test_title), color = NexusPurple)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showTestDialog) {
        TestRouteDialog(
            message = uiState.testMessage,
            onMessageChange = { viewModel.setTestMessage(it) },
            onTest = {
                viewModel.testRoute()
                showTestDialog = false
            },
            onDismiss = { showTestDialog = false }
        )
    }
}

@Composable
private fun StrategyCard(strategy: RoutingStrategy, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) NexusPurple.copy(alpha = 0.15f) else NexusCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (selected) NexusPurple else NexusSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = NexusTextPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strategy.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = NexusTextPrimary
                )
                Text(
                    text = strategy.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = NexusTextTertiary
                )
            }
        }
    }
}

@Composable
private fun ProviderStatusCard(provider: com.nexusai.domain.model.AIProviderConfig) {
    val hasKey = provider.apiKey.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NexusCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (hasKey) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (hasKey) NexusPurple else NexusTextTertiary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = provider.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = NexusTextPrimary
                )
                Text(
                    text = provider.type.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusTextTertiary
                )
            }

            Text(
                text = if (hasKey) stringResource(R.string.ai_router_active) else stringResource(R.string.ai_router_no_key),
                style = MaterialTheme.typography.labelSmall,
                color = if (hasKey) NexusPurple else NexusTextTertiary
            )
        }
    }
}

@Composable
private fun RoutingResultCard(result: com.nexusai.domain.model.AIRoutingResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NexusPurple.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NexusPurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = result.selectedProvider.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NexusTextPrimary
                    )
                    Text(
                        text = result.selectedModel,
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { result.score },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = NexusPurple,
                trackColor = NexusSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result.reason,
                style = MaterialTheme.typography.bodySmall,
                color = NexusTextSecondary
            )

            Text(
                text = "Score: ${(result.score * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = NexusTextTertiary
            )
        }
    }
}

@Composable
private fun TestRouteDialog(
    message: String,
    onMessageChange: (String) -> Unit,
    onTest: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.ai_router_test_title), color = NexusTextPrimary)
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.ai_router_test_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = NexusTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = onMessageChange,
                    placeholder = { Text(stringResource(R.string.ai_router_test_hint), color = NexusTextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NexusPurple,
                        cursorColor = NexusPurple,
                        focusedTextColor = NexusTextPrimary,
                        unfocusedTextColor = NexusTextPrimary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onTest) {
                Text(stringResource(R.string.ai_router_route), color = NexusPurple)
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
