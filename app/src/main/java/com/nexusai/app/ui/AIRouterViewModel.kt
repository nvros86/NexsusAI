package com.nexusai.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.data.ai.AIRouter
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.AIRoutingResult
import com.nexusai.domain.model.RoutingStrategy
import com.nexusai.domain.repository.AIProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AIRouterUiState(
    val providers: List<AIProviderConfig> = emptyList(),
    val strategy: RoutingStrategy = RoutingStrategy.BALANCED,
    val routingResult: AIRoutingResult? = null,
    val isRouting: Boolean = false,
    val testMessage: String = "",
    val lastError: String? = null
)

@HiltViewModel
class AIRouterViewModel @Inject constructor(
    private val aiRouter: AIRouter,
    private val providerRepository: AIProviderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIRouterUiState())
    val uiState: StateFlow<AIRouterUiState> = _uiState.asStateFlow()

    init {
        loadProviders()
    }

    private fun loadProviders() {
        viewModelScope.launch {
            providerRepository.getAllProviders().collect { providers ->
                _uiState.value = _uiState.value.copy(providers = providers)
            }
        }
    }

    fun setStrategy(strategy: RoutingStrategy) {
        _uiState.value = _uiState.value.copy(strategy = strategy)
        runRouting()
    }

    fun runRouting() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRouting = true, lastError = null)
            try {
                val providers = providerRepository.getAllProviders().first()
                val result = aiRouter.route(providers, _uiState.value.strategy)
                _uiState.value = _uiState.value.copy(
                    routingResult = result,
                    isRouting = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRouting = false,
                    lastError = e.message
                )
            }
        }
    }

    fun setTestMessage(message: String) {
        _uiState.value = _uiState.value.copy(testMessage = message)
    }

    fun testRoute() {
        runRouting()
    }
}
