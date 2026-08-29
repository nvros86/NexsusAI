package com.nexusai.feature.editor.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexusai.core.ui.theme.NexusBackground
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextTertiary
import com.nexusai.feature.editor.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadFromUri(it) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = state.fileName.ifEmpty { "Editor" },
                    style = MaterialTheme.typography.titleMedium,
                    color = NexusTextPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = NexusTextPrimary
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        filePickerLauncher.launch(arrayOf("*/*"))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Open file",
                        tint = NexusTextTertiary
                    )
                }
                IconButton(
                    onClick = { viewModel.saveToFile() },
                    enabled = state.isModified
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        tint = if (state.isModified) MaterialTheme.colorScheme.primary else NexusTextTertiary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = NexusBackground
            )
        )

        CodeEditor(
            content = state.content,
            onContentChange = { viewModel.updateContent(it) },
            language = state.language,
            isReadOnly = state.isReadOnly,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Text(
            text = "Ln ${state.cursorLine}, Col ${state.cursorColumn} | ${state.language.uppercase()} | ${state.lineCount} lines",
            style = MaterialTheme.typography.bodySmall,
            color = NexusTextTertiary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
