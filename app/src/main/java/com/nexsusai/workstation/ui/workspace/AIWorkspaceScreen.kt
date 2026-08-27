package com.nexsusai.workstation.ui.workspace

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AIWorkspaceScreen(
    viewModel: AIWorkspaceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.messages) { message ->
                Text(
                    text = message.text,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = state.inputText,
                onValueChange = viewModel::updateInput,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message AI") }
            )
            Button(onClick = viewModel::sendMessage) {
                Text("Send")
            }
        }
    }
}
