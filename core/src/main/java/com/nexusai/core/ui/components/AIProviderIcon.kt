package com.nexusai.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexusai.core.ui.theme.AIBlue
import com.nexusai.core.ui.theme.AIGreen
import com.nexusai.core.ui.theme.AIPurple

@Composable
fun AIProviderIcon(
    providerId: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color.Unspecified
) {
    val (icon, color) = when (providerId.lowercase()) {
        "openai" -> Icons.Default.SmartToy to AIGreen
        "anthropic" -> Icons.Default.SmartToy to AIPurple
        "google" -> Icons.Default.SmartToy to AIBlue
        else -> Icons.Default.SmartToy to AIBlue
    }

    Icon(
        imageVector = icon,
        contentDescription = providerId,
        modifier = modifier.size(size),
        tint = if (tint == Color.Unspecified) color else tint
    )
}
