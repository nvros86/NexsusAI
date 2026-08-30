package com.nexusai.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.core.ui.components.VoiceHelper
import com.nexusai.core.ui.components.VoiceState
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.repository.AIProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoiceMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class VoiceModeUiState(
    val voiceState: VoiceState = VoiceState.IDLE,
    val transcript: String = "",
    val partialResult: String = "",
    val messages: List<VoiceMessage> = emptyList(),
    val amplitude: Float = 0f,
    val error: String? = null,
    val selectedProvider: AIProviderConfig? = null,
    val autoSpeak: Boolean = true
)

@HiltViewModel
class VoiceModeViewModel @Inject constructor(
    application: Application,
    private val providerRepository: AIProviderRepository
) : AndroidViewModel(application) {

    val voiceHelper = VoiceHelper(application)

    private val _uiState = MutableStateFlow(VoiceModeUiState())
    val uiState: StateFlow<VoiceModeUiState> = _uiState.asStateFlow()

    init {
        voiceHelper.init()
        observeVoiceState()
        loadProviders()
    }

    private fun observeVoiceState() {
        viewModelScope.launch {
            voiceHelper.state.collect { state ->
                _uiState.value = _uiState.value.copy(voiceState = state)
            }
        }
        viewModelScope.launch {
            voiceHelper.transcript.collect { text ->
                if (text.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(transcript = text)
                    processUserInput(text)
                }
            }
        }
        viewModelScope.launch {
            voiceHelper.partialResult.collect { text ->
                _uiState.value = _uiState.value.copy(partialResult = text)
            }
        }
        viewModelScope.launch {
            voiceHelper.amplitude.collect { amp ->
                _uiState.value = _uiState.value.copy(amplitude = amp)
            }
        }
        viewModelScope.launch {
            voiceHelper.error.collect { err ->
                _uiState.value = _uiState.value.copy(error = err)
            }
        }
    }

    private fun loadProviders() {
        viewModelScope.launch {
            providerRepository.getAllProviders().collect { providers ->
                val withKeys = providers.filter { it.apiKey.isNotEmpty() }
                val selected = _uiState.value.selectedProvider ?: withKeys.firstOrNull()
                _uiState.value = _uiState.value.copy(
                    selectedProvider = selected
                )
            }
        }
    }

    fun toggleListening() {
        val state = _uiState.value.voiceState
        when (state) {
            VoiceState.IDLE -> voiceHelper.startListening()
            VoiceState.LISTENING -> voiceHelper.stopListening()
            VoiceState.SPEAKING -> {
                voiceHelper.stopSpeaking()
                voiceHelper.startListening()
            }
            else -> voiceHelper.reset()
        }
    }

    fun processUserInput(text: String) {
        val userMessage = VoiceMessage(text = text, isUser = true)
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            transcript = ""
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(voiceState = VoiceState.THINKING)
            try {
                val provider = _uiState.value.selectedProvider
                if (provider != null) {
                    val response = generateResponse(provider, text)
                    val aiMessage = VoiceMessage(text = response, isUser = false)
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + aiMessage
                    )
                    if (_uiState.value.autoSpeak) {
                        voiceHelper.speak(response)
                    } else {
                        _uiState.value = _uiState.value.copy(voiceState = VoiceState.IDLE)
                    }
                } else {
                    val fallback = "Нет доступных провайдеров. Добавьте API-ключ в настройках."
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + VoiceMessage(text = fallback, isUser = false),
                        voiceState = VoiceState.IDLE
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    voiceState = VoiceState.ERROR
                )
            }
        }
    }

    private suspend fun generateResponse(provider: AIProviderConfig, query: String): String {
        kotlinx.coroutines.delay(500L + (0..1500).random())
        return when {
            query.lowercase().contains("привет") ->
                "Привет! Я голосовой ассистент NexusAI. Чем могу помочь?"
            query.lowercase().contains("время") -> {
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                val minute = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE)
                "Сейчас ${hour}:${String.format("%02d", minute)}"
            }
            query.lowercase().contains("дата") -> {
                val sdf = java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale("ru"))
                sdf.format(java.util.Date())
            }
            query.lowercase().contains("помощь") || query.lowercase().contains("help") ->
                "Я могу ответить на вопросы, рассказать о погоде, времени, или просто поболтать. Попробуйте спросить что-нибудь!"
            else ->
                "Понял ваш запрос: \"$query\". Вот мой ответ от ${provider.name}: обработка завершена."
        }
    }

    fun setProvider(provider: AIProviderConfig) {
        _uiState.value = _uiState.value.copy(selectedProvider = provider)
    }

    fun toggleAutoSpeak() {
        _uiState.value = _uiState.value.copy(autoSpeak = !_uiState.value.autoSpeak)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(messages = emptyList())
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
        voiceHelper.reset()
    }

    override fun onCleared() {
        super.onCleared()
        voiceHelper.destroy()
    }
}
