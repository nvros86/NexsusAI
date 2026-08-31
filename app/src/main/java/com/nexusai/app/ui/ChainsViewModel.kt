package com.nexusai.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.domain.model.AutomationChain
import com.nexusai.domain.repository.ChainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChainsViewModel @Inject constructor(
    private val chainRepository: ChainRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChainsUiState())
    val uiState: StateFlow<ChainsUiState> = _uiState.asStateFlow()

    init {
        loadChains()
    }

    private fun loadChains() {
        viewModelScope.launch {
            chainRepository.getAllChains().collect { chains ->
                _uiState.value = _uiState.value.copy(chains = chains)
            }
        }
    }

    fun runChain(chain: AutomationChain) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRunning = true,
                runningChainId = chain.id
            )
            try {
                chainRepository.runChain(chain)
            } catch (_: Exception) {
            } finally {
                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    runningChainId = null
                )
            }
        }
    }

    fun deleteChain(id: String) {
        viewModelScope.launch {
            chainRepository.deleteChain(id)
        }
    }
}
