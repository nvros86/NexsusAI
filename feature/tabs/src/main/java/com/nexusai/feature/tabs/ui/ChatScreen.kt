package com.nexusai.feature.tabs.ui

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.nexusai.core.ui.components.MarkdownText
import com.nexusai.feature.tabs.R
import com.nexusai.core.ui.theme.NexusBackground
import com.nexusai.core.ui.theme.NexusCard
import com.nexusai.core.ui.theme.NexusPurple
import com.nexusai.core.ui.theme.NexusSurface
import com.nexusai.core.ui.theme.NexusSurfaceVariant
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary
import com.nexusai.core.ui.theme.UserMessageBg
import com.nexusai.domain.model.Message
import com.nexusai.domain.model.MessageRole
import com.nexusai.feature.tabs.viewmodel.ChatUiState

@Composable
fun ChatScreen(
    chatState: ChatUiState,
    modifier: Modifier = Modifier,
    providerName: String = "AI",
    onCopyMessage: (String) -> Unit = {}
) {
    val listState = rememberLazyListState()

    LaunchedEffect(chatState.messages.size) {
        if (chatState.messages.isNotEmpty()) {
            listState.animateScrollToItem(chatState.messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        if (chatState.messages.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
            ) {
                items(chatState.messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        providerName = providerName,
                        onCopy = { onCopyMessage(message.content) }
                    )
                }

                if (chatState.isGenerating) {
                    item {
                        GeneratingIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(NexusPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = NexusPurple
                )
            }
            Text(
                text = stringResource(R.string.chat_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                color = NexusTextPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(R.string.chat_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = NexusTextTertiary
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    providerName: String = "AI",
    onCopy: () -> Unit = {}
) {
    val isUser = message.role == MessageRole.USER

    val userMessageDesc = stringResource(R.string.chat_user_message)
    val aiAnswerDesc = stringResource(R.string.chat_ai_answer, providerName)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(NexusPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = NexusPurple
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = providerName,
                    style = MaterialTheme.typography.labelMedium,
                    color = NexusTextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Surface(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .semantics {
                    contentDescription = if (isUser) userMessageDesc else aiAnswerDesc
                },
            shape = RoundedCornerShape(
                topStart = if (isUser) 16.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            color = if (isUser) UserMessageBg else NexusCard
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (isUser) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexusTextPrimary,
                        lineHeight = 22.sp
                    )
                } else {
                    MarkdownText(
                        text = message.content,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (!isUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier.size(14.dp),
                                tint = NexusTextTertiary
                            )
                        }
                        IconButton(
                            onClick = { },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate",
                                modifier = Modifier.size(14.dp),
                                tint = NexusTextTertiary
                            )
                        }
                        IconButton(
                            onClick = { },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Like",
                                modifier = Modifier.size(14.dp),
                                tint = NexusTextTertiary
                            )
                        }
                        IconButton(
                            onClick = { },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbDown,
                                contentDescription = "Dislike",
                                modifier = Modifier.size(14.dp),
                                tint = NexusTextTertiary
                            )
                        }
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = NexusTextTertiary,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(NexusPurple.copy(alpha = 0.2f))
                        .padding(4.dp),
                    tint = NexusPurple
                )
            }
        }
    }
}

@Composable
private fun GeneratingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(NexusPurple.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = NexusPurple
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.chat_generating),
            style = MaterialTheme.typography.bodySmall,
            color = NexusTextTertiary
        )
    }
}
