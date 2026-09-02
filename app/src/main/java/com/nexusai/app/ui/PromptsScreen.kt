package com.nexusai.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.nexusai.domain.model.Prompt
import com.nexusai.domain.model.PromptCategory
import androidx.compose.ui.res.stringResource
import com.nexusai.app.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PromptsScreen(
    onBack: () -> Unit = {},
    viewModel: PromptsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showDetailPrompt by remember { mutableStateOf<Prompt?>(null) }

    LaunchedEffect(uiState.copiedPromptId) {
        if (uiState.copiedPromptId != null) {
            delay(1500)
            viewModel.dismissCopied()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.prompts_title), color = NexusTextPrimary) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = NexusTextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBackground)
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.search(it) },
                placeholder = { Text(stringResource(R.string.prompts_search_hint), color = NexusTextTertiary) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = NexusTextTertiary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = NexusSurfaceVariant,
                    focusedBorderColor = NexusPurple,
                    cursorColor = NexusPurple,
                    focusedTextColor = NexusTextPrimary,
                    unfocusedTextColor = NexusTextPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip(
                    label = stringResource(R.string.prompts_favorites),
                    selected = uiState.showFavoritesOnly,
                    onClick = { viewModel.toggleFavoritesOnly() }
                )
                CategoryChip(
                    label = stringResource(R.string.prompts_all),
                    selected = uiState.selectedCategory == null && !uiState.showFavoritesOnly,
                    onClick = { viewModel.selectCategory(null) }
                )
                PromptCategory.entries.forEach { cat ->
                    CategoryChip(
                        label = "${cat.emoji} ${cat.displayName}",
                        selected = uiState.selectedCategory == cat.name,
                        onClick = { viewModel.selectCategory(cat.name) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.prompts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📝", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState.showFavoritesOnly) stringResource(R.string.prompts_empty_favorites) else stringResource(R.string.prompts_empty),
                        color = NexusTextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.prompts, key = { it.id }) { prompt ->
                    PromptCard(
                        prompt = prompt,
                        onToggleFavorite = { viewModel.toggleFavorite(prompt.id) },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            if (clipboard != null) {
                                val clip = ClipData.newPlainText(prompt.title, prompt.content)
                                clipboard.setPrimaryClip(clip)
                                viewModel.copyPrompt(prompt.id)
                                Toast.makeText(context, context.getString(R.string.prompts_copied), Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDetail = { showDetailPrompt = prompt }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    showDetailPrompt?.let { prompt ->
        AlertDialog(
            onDismissRequest = { showDetailPrompt = null },
            title = {
                Text(
                    text = "${prompt.category.emoji} ${prompt.title}",
                    color = NexusTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column {
                    Text(
                        text = prompt.description,
                        color = NexusTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = prompt.content,
                        color = NexusTextPrimary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    if (clipboard != null) {
                        val clip = ClipData.newPlainText(prompt.title, prompt.content)
                        clipboard.setPrimaryClip(clip)
                        viewModel.copyPrompt(prompt.id)
                    }
                    showDetailPrompt = null
                    Toast.makeText(context, context.getString(R.string.prompts_copied), Toast.LENGTH_SHORT).show()
                }) {
                    Text(stringResource(R.string.prompts_copy), color = NexusPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDetailPrompt = null }) {
                    Text(stringResource(R.string.prompts_close), color = NexusTextSecondary)
                }
            },
            containerColor = NexusCard
        )
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) NexusPurple else NexusSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) NexusTextPrimary else NexusTextSecondary,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PromptCard(
    prompt: Prompt,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit,
    onDetail: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDetail),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NexusCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = prompt.category.emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = prompt.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = NexusTextPrimary
                    )
                    Text(
                        text = prompt.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusTextTertiary
                    )
                }
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (prompt.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(R.string.prompts_favorites),
                        tint = if (prompt.isFavorite) NexusPurple else NexusTextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.prompts_copy),
                        tint = NexusPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = prompt.content,
                style = MaterialTheme.typography.bodySmall,
                color = NexusTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (prompt.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    prompt.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NexusSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = NexusTextTertiary
                            )
                        }
                    }
                }
            }

            if (prompt.usageCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.prompts_usage, prompt.usageCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusTextTertiary
                )
            }
        }
    }
}
