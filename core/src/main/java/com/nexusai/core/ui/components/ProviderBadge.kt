package com.nexusai.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nexusai.core.ui.theme.AIBlue

@Composable
fun ProviderBadge(
    providerName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AIBlue.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AIProviderIcon(
            providerId = providerName,
            modifier = Modifier.size(12.dp),
            tint = AIBlue
        )
        Text(
            text = providerName,
            style = MaterialTheme.typography.labelSmall,
            color = AIBlue,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
