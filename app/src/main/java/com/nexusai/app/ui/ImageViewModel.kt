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
class ImageViewModel @Inject constructor(
    private val providerRepository: AIProviderRepository,
    private val aiProviderManager: AIProviderManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImageGenUiState())
    val uiState: StateFlow<ImageGenUiState> = _uiState.asStateFlow()

    fun setPrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(prompt = prompt)
    }

    fun generate() {
        val prompt = _uiState.value.prompt
        if (prompt.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true, error = null)

            val newImage = GeneratedImage(
                id = System.currentTimeMillis().toString(),
                prompt = prompt
            )
            _uiState.value = _uiState.value.copy(
                images = _uiState.value.images + newImage,
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
                            content = "Сгенерируй URL изображения по описанию: $prompt. Ответь только URL или placeholder."
                        )
                    )
                    val response = aiProvider.sendMessage(
                        messages = messages,
                        model = model,
                        maxTokens = 512
                    )

                    val imageUrl = response.content.trim().let {
                        if (it.startsWith("http")) it else null
                    }

                    _uiState.value = _uiState.value.copy(
                        images = _uiState.value.images.map {
                            if (it.id == newImage.id) it.copy(url = imageUrl) else it
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
            images = _uiState.value.images.map {
                if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
            }
        )
    }

    fun deleteImage(id: String) {
        _uiState.value = _uiState.value.copy(
            images = _uiState.value.images.filter { it.id != id }
        )
    }

    fun clearImages() {
        _uiState.value = _uiState.value.copy(images = emptyList())
    }
}
