package com.nexusai.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.app.R
import com.nexusai.data.ai.AIProviderManager
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.ProviderType
import com.nexusai.domain.repository.AIProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(
    application: Application,
    private val providerRepository: AIProviderRepository,
    private val aiProviderManager: AIProviderManager
) : AndroidViewModel(application) {

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
                var openAIProvider: AIProviderConfig? = null
                providers.collect { list ->
                    openAIProvider = list.firstOrNull {
                        it.apiKey.isNotEmpty() && it.type == ProviderType.OPENAI && it.supportsImages
                    }
                    return@collect
                }

                if (openAIProvider != null) {
                    generateWithDALLE(openAIProvider!!, prompt, newImage.id)
                } else {
                    generateWithPollinations(prompt, newImage.id)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = getApplication<Application>().getString(R.string.error_image_general, e.message ?: ""),
                    isGenerating = false
                )
            }
        }
    }

    private suspend fun generateWithDALLE(
        provider: AIProviderConfig,
        prompt: String,
        imageId: String
    ) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://api.openai.com/v1/images/generations")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer ${provider.apiKey}")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 60000
                connection.readTimeout = 60000

                val body = """{"model":"dall-e-3","prompt":"$prompt","n":1,"size":"1024x1024"}"""
                connection.outputStream.write(body.toByteArray())

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val imageUrl = Regex("\"url\":\"(.*?)\"").find(response)?.groupValues?.get(1)

                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            images = _uiState.value.images.map {
                                if (it.id == imageId) it.copy(url = imageUrl) else it
                            },
                            isGenerating = false
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            error = getApplication<Application>().getString(R.string.error_image_dalle_api, connection.responseCode),
                            isGenerating = false
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        error = getApplication<Application>().getString(R.string.error_image_dalle, e.message ?: ""),
                        isGenerating = false
                    )
                }
            }
        }
    }

    private suspend fun generateWithPollinations(prompt: String, imageId: String) {
        withContext(Dispatchers.IO) {
            try {
                val encodedPrompt = URLEncoder.encode(prompt, "UTF-8")
                val imageUrl = "https://image.pollinations.ai/prompt/$encodedPrompt?width=1024&height=1024&nologo=true"

                val connection = URL(imageUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                connection.instanceFollowRedirects = true

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            images = _uiState.value.images.map {
                                if (it.id == imageId) it.copy(url = imageUrl) else it
                            },
                            isGenerating = false
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            error = getApplication<Application>().getString(R.string.error_image_pollinations, responseCode),
                            isGenerating = false
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        error = getApplication<Application>().getString(R.string.error_image_general, e.message ?: ""),
                        isGenerating = false
                    )
                }
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

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
