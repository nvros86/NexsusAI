package com.nexusai.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexusai.core.ui.components.VoiceState
import com.nexusai.core.ui.theme.NexusBackground
import com.nexusai.core.ui.theme.NexusCard
import com.nexusai.core.ui.theme.NexusPurple
import com.nexusai.core.ui.theme.NexusSurface
import com.nexusai.core.ui.theme.NexusSurfaceVariant
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceModeScreen(
    onBack: () -> Unit = {},
    viewModel: VoiceModeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        TopAppBar(
            title = { Text("Voice Mode", color = NexusTextPrimary) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = NexusTextPrimary
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.toggleAutoSpeak() }) {
                    Icon(
                        imageVector = if (uiState.autoSpeak) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Auto-speak",
                        tint = if (uiState.autoSpeak) NexusPurple else NexusTextTertiary
                    )
                }
                IconButton(onClick = { viewModel.clearMessages() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Очистить",
                        tint = NexusTextTertiary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBackground)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (uiState.messages.isEmpty() && uiState.voiceState == VoiceState.IDLE) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🎤", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Нажмите микрофон",
                        style = MaterialTheme.typography.titleMedium,
                        color = NexusTextSecondary
                    )
                    Text(
                        text = "и начните говорить",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexusTextTertiary
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.messages) { message ->
                        MessageBubble(message = message)
                    }

                    if (uiState.voiceState == VoiceState.THINKING) {
                        item {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = NexusPurple,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Думаю...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NexusTextTertiary
                                )
                            }
                        }
                    }

                    if (uiState.partialResult.isNotEmpty()) {
                        item {
                            Text(
                                text = uiState.partialResult,
                                style = MaterialTheme.typography.bodySmall,
                                color = NexusTextTertiary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(NexusSurface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            val isListening = uiState.voiceState == VoiceState.LISTENING
            val isSpeaking = uiState.voiceState == VoiceState.SPEAKING
            val animatedScale by animateFloatAsState(
                targetValue = if (isListening) 1.2f else 1f,
                animationSpec = tween(300),
                label = "mic_scale"
            )

            if (isListening) {
                VoiceWaveform(amplitude = uiState.amplitude)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Box(
                modifier = Modifier
                    .size((72 * animatedScale).dp)
                    .clip(CircleShape)
                    .background(
                        when (uiState.voiceState) {
                            VoiceState.LISTENING -> NexusPurple
                            VoiceState.THINKING -> NexusPurple.copy(alpha = 0.5f)
                            VoiceState.SPEAKING -> NexusPurple.copy(alpha = 0.7f)
                            VoiceState.ERROR -> MaterialTheme.colorScheme.error
                            else -> NexusPurple
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { viewModel.toggleListening() }) {
                    Icon(
                        imageVector = when (uiState.voiceState) {
                            VoiceState.LISTENING -> Icons.Default.Stop
                            VoiceState.THINKING -> Icons.Default.Mic
                            VoiceState.SPEAKING -> Icons.Default.Stop
                            VoiceState.ERROR -> Icons.Default.Close
                            else -> Icons.Default.Mic
                        },
                        contentDescription = when (uiState.voiceState) {
                            VoiceState.LISTENING -> "Остановить"
                            VoiceState.THINKING -> "Обработка"
                            VoiceState.SPEAKING -> "Остановить"
                            else -> "Начать"
                        },
                        tint = NexusTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (uiState.voiceState) {
                    VoiceState.IDLE -> "Нажмите для начала"
                    VoiceState.LISTENING -> "Слушаю..."
                    VoiceState.THINKING -> "Думаю..."
                    VoiceState.SPEAKING -> "Говорю..."
                    VoiceState.ERROR -> "Ошибка. Нажмите снова"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = NexusTextSecondary
            )

            if (uiState.selectedProvider != null) {
                Text(
                    text = uiState.selectedProvider!!.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusTextTertiary
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: VoiceMessage) {
    val isUser = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(NexusPurple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = NexusPurple,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) NexusPurple else NexusCard
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = NexusTextPrimary
            )
        }
    }
}

@Composable
private fun VoiceWaveform(amplitude: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val barCount = 30
        val barWidth = size.width / barCount * 0.6f
        val spacing = size.width / barCount

        for (i in 0 until barCount) {
            val distFromCenter = kotlin.math.abs(i - barCount / 2).toFloat() / (barCount / 2)
            val heightMultiplier = (1 - distFromCenter * 0.5f) * amplitude
            val barHeight = (8 + heightMultiplier * 32).dp.toPx()
            val x = i * spacing + spacing / 2

            drawLine(
                color = NexusPurple.copy(alpha = 0.6f + heightMultiplier * 0.4f),
                start = Offset(x, size.height / 2 - barHeight / 2),
                end = Offset(x, size.height / 2 + barHeight / 2),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
