package com.nexusai.feature.tabs.ui

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexusai.feature.tabs.R
import com.nexusai.core.ui.theme.NexusBackground
import com.nexusai.core.ui.theme.NexusCard
import com.nexusai.core.ui.theme.NexusCardHover
import com.nexusai.core.ui.theme.NexusPurple
import com.nexusai.core.ui.theme.NexusSurface
import com.nexusai.core.ui.theme.NexusSurfaceLight
import com.nexusai.core.ui.theme.NexusSurfaceVariant
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary
import com.nexusai.domain.model.Tab
import com.nexusai.feature.tabs.viewmodel.TabsViewModel

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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NexusBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
    ) {
        // Search Bar
        item {
            SearchBar(
                query = tabsState.searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                onClear = { viewModel.clearSearch() }
            )
        }

        if (tabsState.isSearchActive) {
            item {
                Text(
                    text = stringResource(R.string.tabs_found_count, tabsState.searchResults.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = NexusTextTertiary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(tabsState.searchResults, key = { it.id }) { tab ->
                SearchResultCard(
                    tab = tab,
                    onClick = {
                        viewModel.setActiveTab(tab.id)
                        viewModel.clearSearch()
                    }
                )
            }
        } else {
        // Category Tabs
        item {
            CategoryTabs()
        }

        // Multi-Tab Section
        item {
            SectionHeader(
                title = stringResource(R.string.tabs_multi_tabs),
                subtitle = stringResource(R.string.tabs_multi_tabs_subtitle),
                showAll = true
            )
        }

        item {
            MultiTabCards(
                tabs = tabsState.tabs,
                activeTabId = activeTabId,
                onTabClick = { viewModel.setActiveTab(it) },
                onNewTab = { viewModel.createTab() }
            )
        }

        // Recent Chats
        item {
            SectionHeader(
                title = stringResource(R.string.tabs_recent_chats),
                showAll = true
            )
        }

        item {
            RecentChatsSection()
        }

        // Quick Actions
        item {
            SectionHeader(
                title = stringResource(R.string.tabs_quick_actions),
                showAll = false
            )
        }

        item {
            QuickActionsSection(
                onNewChat = { viewModel.createTab() }
            )
        }

        // Files and Projects
        item {
            SectionHeader(
                title = stringResource(R.string.tabs_files_projects),
                showAll = true
            )
        }

        item {
            FilesSection()
        }
        } // end else (search inactive)
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
private fun CategoryTabs() {
    val chatLabel = stringResource(R.string.category_chat)
    val categories = listOf(
        chatLabel to Icons.Default.SmartToy,
        stringResource(R.string.category_code) to Icons.Default.Code,
        stringResource(R.string.category_image) to Icons.Default.Image,
        stringResource(R.string.category_video) to Icons.Default.PlayCircle,
        stringResource(R.string.category_agents) to Icons.Default.SmartToy
    )

    var selectedCategory by remember { mutableStateOf(chatLabel) }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories, key = { it.first }) { (name, icon) ->
            val isSelected = name == selectedCategory
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) NexusPurple else NexusSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { selectedCategory = name }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSelected) NexusTextPrimary else NexusTextTertiary
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) NexusTextPrimary else NexusTextTertiary,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null,
    showAll: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = NexusTextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = NexusTextTertiary
                )
            }
        }
        if (showAll) {
            Row(
                modifier = Modifier.clickable { },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.tabs_show_all),
                    style = MaterialTheme.typography.labelMedium,
                    color = NexusPurple
                )
            }
        }
    }
}

@Composable
private fun MultiTabCards(
    tabs: List<com.nexusai.domain.model.Tab>,
    activeTabId: String?,
    onTabClick: (String) -> Unit,
    onNewTab: () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tabs.take(5), key = { it.id }) { tab ->
            Card(
                modifier = Modifier
                    .width(120.dp)
                    .clickable { onTabClick(tab.id) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (tab.id == activeTabId) NexusPurple.copy(alpha = 0.2f) else NexusCard
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NexusPurple.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = NexusPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = NexusTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.category_chat),
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusTextTertiary
                    )
                }
            }
        }

        // New Tab Card
        item {
            Card(
                modifier = Modifier
                    .width(120.dp)
                    .clickable { onNewTab() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = NexusCard
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NexusSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New tab",
                            tint = NexusTextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.tabs_new_tab),
                        style = MaterialTheme.typography.labelMedium,
                        color = NexusTextTertiary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentChatsSection() {
    val recentChats = listOf(
        Triple("Архитектура Android приложения", "GPT-4o", "10:45"),
        Triple("Оптимизация кода", "Claude 3.5", "Вчера"),
        Triple("Исследование AI трендов", "Gemini 1.5", "Вчера"),
        Triple("Генерация изображений", "Midjourney", "2 дня назад"),
        Triple("Анализ данных", "GPT-4o", "3 дня назад")
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NexusCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            recentChats.forEach { (title, provider, time) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NexusSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = NexusTextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = NexusTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = provider,
                            style = MaterialTheme.typography.bodySmall,
                            color = NexusTextTertiary
                        )
                    }
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusTextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onNewChat: () -> Unit
) {
    val actions = listOf(
        Triple(stringResource(R.string.quick_action_new_chat), stringResource(R.string.quick_action_new_chat_desc), Icons.Default.SmartToy) to onNewChat,
        Triple(stringResource(R.string.quick_action_write_code), stringResource(R.string.quick_action_write_code_desc), Icons.Default.Code) to { },
        Triple(stringResource(R.string.quick_action_create_image), stringResource(R.string.quick_action_create_image_desc), Icons.Default.Image) to { },
        Triple(stringResource(R.string.quick_action_run_agent), stringResource(R.string.quick_action_run_agent_desc), Icons.Default.SmartToy) to { }
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(actions, key = { it.first.first }) { (action, onClick) ->
            val (title, subtitle, icon) = action
            Card(
                modifier = Modifier
                    .width(140.dp)
                    .clickable { onClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NexusCard)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NexusPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = NexusPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = NexusTextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusTextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun FilesSection() {
    val files = listOf(
        Triple("architecture.md", "24 KB", "doc"),
        Triple("app.zip", "12.4 MB", "zip"),
        Triple("design.png", "1.2 MB", "image"),
        Triple("video.mp4", "45.2 MB", "video")
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(files, key = { it.first }) { (name, size, type) ->
            Card(
                modifier = Modifier.width(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NexusCard)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NexusSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (type) {
                                "doc" -> Icons.Default.Code
                                "zip" -> Icons.Default.Code
                                "image" -> Icons.Default.Image
                                "video" -> Icons.Default.PlayCircle
                                else -> Icons.Default.Code
                            },
                            contentDescription = null,
                            tint = NexusTextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelMedium,
                        color = NexusTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = size,
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusTextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(stringResource(R.string.search_placeholder), color = NexusTextTertiary)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.action_search),
                tint = NexusTextTertiary
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.action_clear),
                        tint = NexusTextTertiary
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NexusPurple,
            unfocusedBorderColor = NexusSurfaceVariant,
            focusedContainerColor = NexusSurface,
            unfocusedContainerColor = NexusSurface,
            focusedTextColor = NexusTextPrimary,
            unfocusedTextColor = NexusTextPrimary
        )
    )
}

@Composable
private fun SearchResultCard(
    tab: Tab,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = NexusCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = NexusPurple,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = NexusTextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            if (tab.messages.isNotEmpty()) {
                val lastMessage = tab.messages.lastOrNull()
                if (lastMessage != null) {
                    Text(
                        text = lastMessage.content.take(120),
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusTextTertiary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Text(
                text = stringResource(R.string.tabs_messages, tab.messages.size),
                style = MaterialTheme.typography.labelSmall,
                color = NexusTextTertiary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
