package com.nexusai.app.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nexusai.core.ui.components.file.FileHelper
import com.nexusai.core.ui.theme.NexusBackground
import com.nexusai.core.ui.theme.NexusCard
import com.nexusai.core.ui.theme.NexusPurple
import com.nexusai.core.ui.theme.NexusSurface
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary
import java.io.File

data class ManagedFile(
    val id: String,
    val name: String,
    val uri: Uri,
    val mimeType: String,
    val size: Long,
    val addedAt: Long = System.currentTimeMillis()
)

enum class FileFilter(val displayName: String, val emoji: String) {
    ALL("Все", "📁"),
    IMAGES("Изображения", "🖼️"),
    DOCUMENTS("Документы", "📄"),
    CODE("Код", "💻"),
    MEDIA("Медиа", "🎬"),
    OTHER("Другие", "📎")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilesScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val files = remember { mutableStateListOf<ManagedFile>() }
    var selectedFilter by remember { mutableStateOf(FileFilter.ALL) }
    var showDeleteDialog by remember { mutableStateOf<ManagedFile?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val info = FileHelper.getFileInfo(context, it)
            if (info != null) {
                files.add(
                    ManagedFile(
                        id = System.currentTimeMillis().toString(),
                        name = info.name,
                        uri = it,
                        mimeType = info.mimeType,
                        size = info.size
                    )
                )
            }
        }
    }

    val filteredFiles = remember(files, selectedFilter) {
        when (selectedFilter) {
            FileFilter.ALL -> files
            FileFilter.IMAGES -> files.filter { FileHelper.isImage(it.mimeType) }
            FileFilter.DOCUMENTS -> files.filter {
                it.mimeType.contains("pdf") || it.mimeType.contains("document") ||
                        it.mimeType.contains("text/") || it.mimeType.contains("sheet")
            }
            FileFilter.CODE -> files.filter {
                FileHelper.isText(it.mimeType) || it.mimeType.contains("json") ||
                        it.mimeType.contains("xml")
            }
            FileFilter.MEDIA -> files.filter {
                it.mimeType.startsWith("video/") || it.mimeType.startsWith("audio/")
            }
            FileFilter.OTHER -> files.filter { file ->
                !FileHelper.isImage(file.mimeType) &&
                        !file.mimeType.contains("pdf") &&
                        !file.mimeType.contains("text/") &&
                        !file.mimeType.startsWith("video/") &&
                        !file.mimeType.startsWith("audio/")
            }
        }
    }

    val totalSize = remember(files) { files.sumOf { it.size } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        TopAppBar(
            title = { Text("Файлы", color = NexusTextPrimary) },
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

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            StorageBar(
                usedBytes = totalSize,
                maxBytes = 100L * 1024 * 1024,
                fileCount = files.size
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FileFilter.entries.forEach { filter ->
                    FilterChip(
                        label = "${filter.emoji} ${filter.displayName}",
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredFiles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📂", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (files.isEmpty()) "Нет файлов" else "Нет файлов в этой категории",
                        color = NexusTextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredFiles, key = { it.id }) { file ->
                    FileGridItem(
                        file = file,
                        onClick = { showFilePreview(context, file) },
                        onShare = { shareFile(context, file) },
                        onDelete = { showDeleteDialog = file }
                    )
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = {
                    filePickerLauncher.launch(
                        arrayOf(
                            "image/*",
                            "video/*",
                            "audio/*",
                            "application/pdf",
                            "text/*",
                            "application/json",
                            "application/zip"
                        )
                    )
                },
                modifier = Modifier.padding(16.dp),
                containerColor = NexusPurple,
                contentColor = NexusTextPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить файл"
                )
            }
        }
    }

    showDeleteDialog?.let { file ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Удалить файл?", color = NexusTextPrimary) },
            text = { Text("Удалить \"${file.name}\"?", color = NexusTextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    files.removeAll { it.id == file.id }
                    showDeleteDialog = null
                }) {
                    Text("Удалить", color = NexusPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Отмена", color = NexusTextSecondary)
                }
            },
            containerColor = NexusCard
        )
    }
}

@Composable
private fun StorageBar(usedBytes: Long, maxBytes: Long, fileCount: Int) {
    val progress = (usedBytes.toFloat() / maxBytes).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NexusCard)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Хранилище",
                color = NexusTextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$fileCount файлов",
                color = NexusTextTertiary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = NexusPurple,
            trackColor = NexusSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${FileHelper.formatFileSize(usedBytes)} / ${FileHelper.formatFileSize(maxBytes)}",
            color = NexusTextTertiary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
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

@Composable
private fun FileGridItem(
    file: ManagedFile,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val isImage = FileHelper.isImage(file.mimeType)

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NexusCard)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        if (isImage) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(file.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = file.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NexusPurple.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = fileIcon(file.mimeType),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = NexusPurple
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = file.name,
            color = NexusTextPrimary,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = FileHelper.formatFileSize(file.size),
            color = NexusTextTertiary,
            style = MaterialTheme.typography.labelSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onShare, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Поделиться",
                    modifier = Modifier.size(14.dp),
                    tint = NexusTextTertiary
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    modifier = Modifier.size(14.dp),
                    tint = NexusTextTertiary
                )
            }
        }
    }
}

private fun fileIcon(mimeType: String): ImageVector {
    return when {
        FileHelper.isImage(mimeType) -> Icons.Default.Image
        FileHelper.isText(mimeType) -> Icons.Default.Description
        mimeType.startsWith("video/") || mimeType.startsWith("audio/") -> Icons.AutoMirrored.Filled.InsertDriveFile
        else -> Icons.AutoMirrored.Filled.InsertDriveFile
    }
}

private fun showFilePreview(context: android.content.Context, file: ManagedFile) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(file.uri, file.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Не удалось открыть файл", Toast.LENGTH_SHORT).show()
    }
}

private fun shareFile(context: android.content.Context, file: ManagedFile) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = file.mimeType
            putExtra(Intent.EXTRA_STREAM, file.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Поделиться файлом"))
    } catch (e: Exception) {
        Toast.makeText(context, "Не удалось поделиться", Toast.LENGTH_SHORT).show()
    }
}
