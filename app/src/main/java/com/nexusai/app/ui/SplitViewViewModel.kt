package com.nexusai.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.ComparisonMode
import com.nexusai.domain.model.SplitResult
import com.nexusai.domain.model.SplitSession
import com.nexusai.domain.repository.AIProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplitViewUiState(
    val query: String = "",
    val comparisonMode: ComparisonMode = ComparisonMode.TWO,
    val selectedProviders: List<AIProviderConfig> = emptyList(),
    val availableProviders: List<AIProviderConfig> = emptyList(),
    val results: List<SplitResult> = emptyList(),
    val isRunning: Boolean = false,
    val sessions: List<SplitSession> = emptyList(),
    val selectedSession: SplitSession? = null
)

@HiltViewModel
class SplitViewViewModel @Inject constructor(
    private val providerRepository: AIProviderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplitViewUiState())
    val uiState: StateFlow<SplitViewUiState> = _uiState.asStateFlow()

    init {
        loadProviders()
    }

    private fun loadProviders() {
        viewModelScope.launch {
            providerRepository.getAllProviders().collect { providers ->
                val withKeys = providers.filter { it.apiKey.isNotEmpty() }
                _uiState.value = _uiState.value.copy(
                    availableProviders = withKeys,
                    selectedProviders = withKeys.take(_uiState.value.comparisonMode.count)
                )
            }
        }
    }

    fun setQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun setComparisonMode(mode: ComparisonMode) {
        val providers = _uiState.value.availableProviders.take(mode.count)
        _uiState.value = _uiState.value.copy(
            comparisonMode = mode,
            selectedProviders = providers
        )
    }

    fun toggleProvider(provider: AIProviderConfig) {
        val current = _uiState.value.selectedProviders.toMutableList()
        if (current.contains(provider)) {
            if (current.size > 1) current.remove(provider)
        } else {
            if (current.size < _uiState.value.comparisonMode.count) {
                current.add(provider)
            }
        }
        _uiState.value = _uiState.value.copy(selectedProviders = current)
    }

    fun runComparison() {
        val state = _uiState.value
        if (state.query.isBlank() || state.selectedProviders.isEmpty()) return

        viewModelScope.launch {
            val results = state.selectedProviders.map { provider ->
                SplitResult(
                    providerId = provider.id,
                    providerName = provider.name,
                    modelName = provider.defaultModel.ifEmpty { provider.models.firstOrNull() ?: "default" },
                    isLoading = true
                )
            }
            _uiState.value = state.copy(results = results, isRunning = true)

            val finalResults = state.selectedProviders.mapIndexed { index, provider ->
                val startTime = System.currentTimeMillis()
                try {
                    val type = provider.type.name
                    val model = provider.defaultModel.ifEmpty { provider.models.firstOrNull() ?: "default" }
                    val response = simulateResponse(provider, state.query, type)
                    val latency = System.currentTimeMillis() - startTime

                    SplitResult(
                        providerId = provider.id,
                        providerName = provider.name,
                        modelName = model,
                        response = response,
                        latencyMs = latency,
                        tokensUsed = response.length / 4
                    )
                } catch (e: Exception) {
                    SplitResult(
                        providerId = provider.id,
                        providerName = provider.name,
                        modelName = provider.defaultModel.ifEmpty { "default" },
                        error = e.message ?: "Unknown error"
                    )
                }
            }

            _uiState.value = _uiState.value.copy(
                results = finalResults,
                isRunning = false
            )
        }
    }

    private suspend fun simulateResponse(
        provider: AIProviderConfig,
        query: String,
        type: String
    ): String {
        kotlinx.coroutines.delay(500 + (0..2000).random())

        return when {
            query.lowercase().contains("привет") || query.lowercase().contains("hello") ->
                "Привет! Я ${provider.name}. Рад помочь тебе с любыми задачами."
            query.lowercase().contains("код") || query.lowercase().contains("code") ->
                "```\n// Ответ от ${provider.name}\nfun main() {\n    println(\"Привет, мир!\")\n}\n```\n\nЭто базовый пример на Kotlin."
            query.lowercase().contains("объясни") || query.lowercase().contains("explain") ->
                "Конечно! Позволь объяснить.\n\n${query.take(50)} — это важная тема. Вот основные моменты:\n\n1. Первый аспект\n2. Второй аспект\n3. Третий аспект\n\nХочешь подробнее?"
            else ->
                "Это отличный вопрос! Вот мой ответ от ${provider.name}:\n\nОбработка запроса завершена. Для более детального ответа уточни задачу."
        }
    }

    fun rateResult(providerId: String, rating: Int) {
        val updated = _uiState.value.results.map {
            if (it.providerId == providerId) it.copy(rating = rating) else it
        }
        _uiState.value = _uiState.value.copy(results = updated)
    }

    fun selectWinner(providerId: String) {
        _uiState.value = _uiState.value.copy(
            selectedSession = _uiState.value.selectedSession?.copy(selectedWinner = providerId)
        )
    }
}
