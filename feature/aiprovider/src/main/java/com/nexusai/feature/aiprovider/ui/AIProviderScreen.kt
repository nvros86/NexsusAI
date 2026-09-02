package com.nexusai.feature.aiprovider.ui

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.nexusai.feature.aiprovider.R
import com.nexusai.core.ui.components.AIProviderIcon
import com.nexusai.core.ui.theme.NexusBackground
import com.nexusai.core.ui.theme.NexusCard
import com.nexusai.core.ui.theme.NexusPurple
import com.nexusai.core.ui.theme.NexusSurface
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIProviderScreen(
    providerId: String,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AIProviderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = state.provider?.name ?: stringResource(R.string.ai_provider_title),
                    color = NexusTextPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = NexusTextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = NexusBackground
            )
        )

        state.provider?.let { provider ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
            ) {
                item {
                    ProviderHeader(
                        name = provider.name,
                        type = provider.type.name,
                        isFavorite = provider.isFavorite
                    )
                }

                item {
                    InfoSection(
                        title = stringResource(R.string.ai_provider_info),
                        items = listOf(
                            stringResource(R.string.ai_provider_type) to provider.type.name,
                            "Base URL" to provider.baseUrl,
                            stringResource(R.string.ai_provider_default_model) to (provider.defaultModel.ifEmpty { stringResource(R.string.ai_provider_not_set) }),
                            stringResource(R.string.ai_provider_max_tokens) to provider.maxTokens.toString(),
                            stringResource(R.string.ai_provider_temperature) to provider.temperature.toString()
                        )
                    )
                }

                if (provider.models.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.ai_provider_available_models),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = NexusTextPrimary
                        )
                    }

                    items(provider.models, key = { it }) { model ->
                        ModelCard(
                            name = model,
                            isDefault = model == provider.defaultModel
                        )
                    }
                }

                item {
                    CapabilitiesSection(
                        supportsImages = provider.supportsImages,
                        supportsFiles = provider.supportsFiles,
                        supportsStreaming = provider.supportsStreaming
                    )
                }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.ai_provider_loading),
                color = NexusTextTertiary
            )
        }
    }
}

@Composable
private fun ProviderHeader(
    name: String,
    type: String,
    isFavorite: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        AIProviderIcon(
            providerId = type.lowercase(),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = NexusTextPrimary
            )
            Text(
                text = type,
                style = MaterialTheme.typography.bodyMedium,
                color = NexusTextSecondary
            )
        }
        if (isFavorite) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = stringResource(R.string.prompts_favorites),
                tint = NexusPurple,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    items: List<Pair<String, String>>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = NexusTextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexusTextTertiary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexusTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelCard(
    name: String,
    isDefault: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDefault) NexusPurple.copy(alpha = 0.1f) else NexusCard
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
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = NexusTextPrimary,
                modifier = Modifier.weight(1f)
            )
            if (isDefault) {
                Text(
                    text = stringResource(R.string.ai_provider_default),
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusPurple,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CapabilitiesSection(
    supportsImages: Boolean,
    supportsFiles: Boolean,
    supportsStreaming: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.ai_provider_capabilities),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = NexusTextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            CapabilityRow(stringResource(R.string.ai_provider_images), supportsImages)
            CapabilityRow(stringResource(R.string.ai_provider_files), supportsFiles)
            CapabilityRow(stringResource(R.string.ai_provider_streaming), supportsStreaming)
        }
    }
}

@Composable
private fun CapabilityRow(
    name: String,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (enabled) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (enabled) NexusPurple else NexusTextTertiary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) NexusTextPrimary else NexusTextTertiary
        )
    }
}
