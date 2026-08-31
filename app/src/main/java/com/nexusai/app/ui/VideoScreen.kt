package com.nexusai.app.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
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
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary

data class GeneratedVideo(
    val id: String,
    val prompt: String,
    val url: String? = null,
    val duration: String = "0:00",
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

data class VideoGenUiState(
    val prompt: String = "",
    val isGenerating: Boolean = false,
    val videos: List<GeneratedVideo> = emptyList(),
    val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(
    onBack: () -> Unit = {},
    viewModel: VideoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        TopAppBar(
            title = {
                Text("Генерация видео", color = NexusTextPrimary)
            },
            actions = {
                IconButton(onClick = { viewModel.clearVideos() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Очистить",
                        tint = NexusTextTertiary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBackground)
        )

        if (state.videos.isEmpty() && !state.isGenerating) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = NexusTextTertiary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Создайте видео",
                        style = MaterialTheme.typography.titleMedium,
                        color = NexusTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Опишите сцену для видео",
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
                items(state.videos.reversed()) { video ->
                    VideoCard(
                        video = video,
                        onFavorite = { viewModel.toggleFavorite(video.id) },
                        onDelete = { viewModel.deleteVideo(video.id) }
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
                        text = "Генерация видео...",
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
                    Text("Опишите сцену...", color = NexusTextTertiary)
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
private fun VideoCard(
    video: GeneratedVideo,
    onFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(NexusSurface),
                contentAlignment = Alignment.Center
            ) {
                if (video.url != null) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Воспроизвести",
                        modifier = Modifier.size(48.dp),
                        tint = NexusPurple
                    )
                } else if (video.url == null && video.prompt.isNotEmpty()) {
                    CircularProgressIndicator(color = NexusPurple, modifier = Modifier.size(32.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = NexusTextTertiary.copy(alpha = 0.3f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexusTextSecondary,
                        maxLines = 2
                    )
                    Text(
                        text = video.duration,
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusTextTertiary
                    )
                }
                IconButton(onClick = onFavorite, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Избранное",
                        tint = if (video.isFavorite) NexusPurple else NexusTextTertiary,
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
