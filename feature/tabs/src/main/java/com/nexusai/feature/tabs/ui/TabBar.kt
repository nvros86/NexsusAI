package com.nexusai.feature.tabs.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexusai.core.ui.theme.NexusBackground
import com.nexusai.core.ui.theme.NexusCard
import com.nexusai.core.ui.theme.NexusPurple
import com.nexusai.core.ui.theme.NexusSurface
import com.nexusai.core.ui.theme.NexusSurfaceVariant
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextTertiary
import com.nexusai.domain.model.Tab

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabBar(
    tabs: List<Tab>,
    activeTabId: String?,
    onTabClick: (String) -> Unit,
    onTabClose: (String) -> Unit,
    onNewTab: () -> Unit,
    onRename: (String, String) -> Unit,
    onDuplicate: (String) -> Unit,
    onCloseAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var contextMenuTabId by remember { mutableStateOf<String?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(NexusSurface)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEach { tab ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { dismissValue ->
                    if (dismissValue == SwipeToDismissBoxValue.StartToEnd || 
                        dismissValue == SwipeToDismissBoxValue.EndToStart) {
                        onTabClose(tab.id)
                        true
                    } else {
                        false
                    }
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    val color by animateColorAsState(
                        targetValue = when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.StartToEnd -> Color.Red.copy(alpha = 0.8f)
                            SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                            SwipeToDismissBoxValue.Settled -> Color.Transparent
                        },
                        label = "dismiss_color"
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) 1.2f else 0.8f,
                        label = "dismiss_scale"
                    )
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.scale(scale),
                            tint = Color.White
                        )
                    }
                }
            ) {
                Box {
                    Row(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (tab.id == activeTabId)
                                    NexusPurple.copy(alpha = 0.2f)
                                else
                                    NexusCard
                            )
                            .combinedClickable(
                                onClick = { onTabClick(tab.id) },
                                onLongClick = {
                                    contextMenuTabId = tab.id
                                }
                            )
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = tab.title,
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                            color = if (tab.id == activeTabId)
                                NexusPurple
                            else
                                NexusTextPrimary,
                            fontWeight = if (tab.id == activeTabId) FontWeight.Medium else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        IconButton(
                            onClick = { onTabClose(tab.id) },
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close tab",
                                modifier = Modifier.size(12.dp),
                                tint = NexusTextTertiary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = contextMenuTabId == tab.id,
                        onDismissRequest = { contextMenuTabId = null },
                        containerColor = NexusSurface
                    ) {
                        DropdownMenuItem(
                            text = { Text("Переименовать", color = NexusTextPrimary) },
                            onClick = {
                                renameText = tab.title
                                showRenameDialog = true
                                contextMenuTabId = null
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Дублировать", color = NexusTextPrimary) },
                            onClick = {
                                onDuplicate(tab.id)
                                contextMenuTabId = null
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Закрыть", color = NexusTextPrimary) },
                            onClick = {
                                onTabClose(tab.id)
                                contextMenuTabId = null
                            }
                        )
                        if (tabs.size > 1) {
                            DropdownMenuItem(
                                text = { Text("Закрыть все", color = Color.Red) },
                                onClick = {
                                    onCloseAll()
                                    contextMenuTabId = null
                                }
                            )
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = onNewTab,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NexusPurple.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New tab",
                tint = NexusPurple
            )
        }
    }

    if (showRenameDialog) {
        RenameDialog(
            currentName = renameText,
            onConfirm = { newName ->
                contextMenuTabId?.let { onRename(it, newName) }
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false }
        )
    }
}
