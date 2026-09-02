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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nexusai.feature.tabs.R
import com.nexusai.core.ui.theme.NexusCard
import com.nexusai.core.ui.theme.NexusPurple
import com.nexusai.core.ui.theme.NexusSurface
import com.nexusai.core.ui.theme.NexusSurfaceVariant
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary
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
        containerColor = NexusSurface,
        title = {
            Text(
                stringResource(R.string.provider_selector_title),
                color = NexusTextPrimary
            )
        },
        text = {
            if (providers.isEmpty()) {
                Text(
                    text = stringResource(R.string.provider_selector_empty),
                    color = NexusTextTertiary
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
                Text(stringResource(R.string.action_close), color = NexusTextTertiary)
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
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected)
            NexusPurple.copy(alpha = 0.2f)
        else
            NexusCard
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
                    color = NexusTextPrimary
                )
                Text(
                    text = provider.type.name,
                    color = NexusTextTertiary
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = NexusPurple
                )
            }
        }
    }
}

@Composable
private fun providerTypeColor(type: ProviderType) = when (type) {
    ProviderType.OPENAI -> NexusPurple
    ProviderType.ANTHROPIC -> com.nexusai.core.ui.theme.NexusCyan
    ProviderType.GEMINI -> com.nexusai.core.ui.theme.NexusBlue
    ProviderType.STABILITY -> com.nexusai.core.ui.theme.NexusGreen
    ProviderType.ELEVENLABS -> com.nexusai.core.ui.theme.NexusCyan
    ProviderType.RUNWAY -> NexusPurple
    ProviderType.CUSTOM -> NexusTextTertiary
    ProviderType.LOCAL -> com.nexusai.core.ui.theme.NexusBlue
}
