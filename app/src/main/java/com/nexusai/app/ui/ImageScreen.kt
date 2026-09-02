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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nexusai.core.ui.theme.NexusBackground
import com.nexusai.core.ui.theme.NexusCard
import com.nexusai.core.ui.theme.NexusPurple
import com.nexusai.core.ui.theme.NexusSurface
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary

data class GeneratedImage(
    val id: String,
    val prompt: String,
    val url: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

data class ImageGenUiState(
    val prompt: String = "",
    val isGenerating: Boolean = false,
    val images: List<GeneratedImage> = emptyList(),
    val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageScreen(
    onBack: () -> Unit = {},
    viewModel: ImageViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showPromptDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        TopAppBar(
            title = {
                Text("Генерация изображений", color = NexusTextPrimary)
            },
            actions = {
                IconButton(onClick = { viewModel.clearImages() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Очистить",
                        tint = NexusTextTertiary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBackground)
        )

        state.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp),
                onClick = { viewModel.dismissError() }
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (state.images.isEmpty() && !state.isGenerating) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = NexusTextTertiary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Создайте изображение",
                        style = MaterialTheme.typography.titleMedium,
                        color = NexusTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Опишите, что вы хотите увидеть",
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
            ) {
                items(state.images.reversed()) { image ->
                    GeneratedImageCard(
                        image = image,
                        onFavorite = { viewModel.toggleFavorite(image.id) },
                        onDelete = { viewModel.deleteImage(image.id) }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (state.isGenerating) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = NexusPurple,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Генерация...",
                        color = NexusTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = state.prompt,
                onValueChange = { viewModel.setPrompt(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Опишите изображение...", color = NexusTextTertiary)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NexusPurple,
                    unfocusedBorderColor = NexusSurface,
                    focusedContainerColor = NexusCard,
                    unfocusedContainerColor = NexusCard
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            FloatingActionButton(
                onClick = { viewModel.generate() },
                modifier = Modifier.fillMaxWidth(),
                containerColor = NexusPurple,
                contentColor = NexusTextPrimary
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Генерировать"
                )
            }
        }
    }
}

@Composable
private fun GeneratedImageCard(
    image: GeneratedImage,
    onFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            if (image.url != null) {
                AsyncImage(
                    model = image.url,
                    contentDescription = image.prompt,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(NexusSurface),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NexusPurple)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = image.prompt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NexusTextSecondary,
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )
                IconButton(onClick = onFavorite, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Избранное",
                        tint = if (image.isFavorite) NexusPurple else NexusTextTertiary,
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
}
