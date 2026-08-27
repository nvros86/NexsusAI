package com.nexusai.feature.tabs.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.ProviderType

@Composable
fun AIProviderSelector(
    providers: List<AIProviderConfig>,
    selectedProviderId: String?,
    onProviderSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select AI Provider") },
        text = {
            if (providers.isEmpty()) {
                Text(
                    text = "No providers configured. Add one in Settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(providers, key = { it.id }) { provider ->
                        ProviderItem(
                            provider = provider,
                            isSelected = provider.id == selectedProviderId,
                            onClick = { onProviderSelected(provider.id) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun ProviderItem(
    provider: AIProviderConfig,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = providerTypeColor(provider.type)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = provider.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = provider.type.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun providerTypeColor(type: ProviderType) = when (type) {
    ProviderType.OPENAI -> MaterialTheme.colorScheme.primary
    ProviderType.ANTHROPIC -> MaterialTheme.colorScheme.tertiary
    ProviderType.GEMINI -> MaterialTheme.colorScheme.secondary
    ProviderType.STABILITY -> MaterialTheme.colorScheme.error
    ProviderType.ELEVENLABS -> MaterialTheme.colorScheme.tertiary
    ProviderType.RUNWAY -> MaterialTheme.colorScheme.primary
    ProviderType.CUSTOM -> MaterialTheme.colorScheme.onSurfaceVariant
    ProviderType.LOCAL -> MaterialTheme.colorScheme.secondary
}
