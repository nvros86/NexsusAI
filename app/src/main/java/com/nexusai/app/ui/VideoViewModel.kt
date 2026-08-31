package com.nexusai.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.data.ai.AIProviderManager
import com.nexusai.domain.ai.ChatMessage
import com.nexusai.domain.ai.MessageRole
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.repository.AIProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val providerRepository: AIProviderRepository,
    private val aiProviderManager: AIProviderManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoGenUiState())
    val uiState: StateFlow<VideoGenUiState> = _uiState.asStateFlow()

    fun setPrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(prompt = prompt)
    }

    fun generate() {
        val prompt = _uiState.value.prompt
        if (prompt.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true, error = null)

            val newVideo = GeneratedVideo(
                id = System.currentTimeMillis().toString(),
                prompt = prompt
            )
            _uiState.value = _uiState.value.copy(
                videos = _uiState.value.videos + newVideo,
                prompt = ""
            )

            try {
                val providers = providerRepository.getAllProviders()
                var provider: AIProviderConfig? = null
                providers.collect { list ->
                    provider = list.firstOrNull { it.apiKey.isNotEmpty() }
                    return@collect
                }

                if (provider != null) {
                    val aiProvider = aiProviderManager.getProvider(provider!!)
                    val model = provider!!.defaultModel.ifEmpty { provider!!.models.firstOrNull() ?: "default" }
                    val messages = listOf(
                        ChatMessage(
                            role = MessageRole.USER,
                            content = "Сгенерируй URL видео по описанию: $prompt. Ответь только URL или placeholder."
                        )
                    )
                    val response = aiProvider.sendMessage(
                        messages = messages,
                        model = model,
                        maxTokens = 512
                    )

                    val videoUrl = response.content.trim().let {
                        if (it.startsWith("http")) it else null
                    }

                    _uiState.value = _uiState.value.copy(
                        videos = _uiState.value.videos.map {
                            if (it.id == newVideo.id) it.copy(url = videoUrl, duration = "0:05") else it
                        },
                        isGenerating = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Нет доступных провайдеров",
                        isGenerating = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isGenerating = false
                )
            }
        }
    }

    fun toggleFavorite(id: String) {
        _uiState.value = _uiState.value.copy(
            videos = _uiState.value.videos.map {
                if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
            }
        )
    }

    fun deleteVideo(id: String) {
        _uiState.value = _uiState.value.copy(
            videos = _uiState.value.videos.filter { it.id != id }
        )
    }

    fun clearVideos() {
        _uiState.value = _uiState.value.copy(videos = emptyList())
    }
}
