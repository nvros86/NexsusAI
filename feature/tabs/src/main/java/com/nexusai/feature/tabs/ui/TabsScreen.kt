package com.nexusai.feature.tabs.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabsScreen(
    viewModel: TabsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val tabsState by viewModel.tabsState.collectAsState()
    val chatStates by viewModel.chatStates.collectAsState()
    val providers by viewModel.providers.collectAsState()

    var showProviderSelector by remember { mutableStateOf(false) }
    var selectedTabForProvider by remember { mutableStateOf<String?>(null) }

    val activeTabId = tabsState.activeTabId
    val activeChatState = activeTabId?.let { chatStates[it] }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NexsusAI") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(
                        onClick = {
                            selectedTabForProvider = activeTabId
                            showProviderSelector = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch provider"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabBar(
                tabs = tabsState.tabs,
                activeTabId = activeTabId,
                onTabClick = { viewModel.setActiveTab(it) },
                onTabClose = { viewModel.deleteTab(it) },
                onNewTab = { viewModel.createTab() },
                onRename = { id, name -> viewModel.renameTab(id, name) },
                onDuplicate = { viewModel.duplicateTab(it) },
                modifier = Modifier.fillMaxWidth()
            )

            if (activeTabId != null && activeChatState != null) {
                ChatScreen(
                    chatState = activeChatState,
                    modifier = Modifier.weight(1f)
                )

                MessageInput(
                    value = activeChatState.inputText,
                    onValueChange = { viewModel.updateInput(activeTabId, it) },
                    onSend = { viewModel.sendMessage(activeTabId) },
                    onAttachFile = { /* TODO: File picker */ },
                    onAttachImage = { /* TODO: Image picker */ },
                    onVoiceInput = { /* TODO: Voice input */ },
                    isGenerating = activeChatState.isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            } else {
                EmptyTabContent(
                    onNewTab = { viewModel.createTab() }
                )
            }
        }
    }

    if (showProviderSelector && selectedTabForProvider != null) {
        AIProviderSelector(
            providers = providers,
            selectedProviderId = activeChatState?.currentProvider?.id,
            onProviderSelected = { providerId ->
                viewModel.setTabProvider(selectedTabForProvider!!, providerId)
                showProviderSelector = false
            },
            onDismiss = { showProviderSelector = false }
        )
    }
}

@Composable
private fun EmptyTabContent(
    onNewTab: () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "No tabs open",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            androidx.compose.material3.FilledTonalButton(
                onClick = onNewTab
            ) {
                Text("Create new tab")
            }
        }
    }
}
