package com.nexusai.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.app.R
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
    application: Application,
    private val chainRepository: ChainRepository
) : AndroidViewModel(application) {

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
                runningChainId = chain.id,
                error = null
            )
            try {
                chainRepository.runChain(chain)
            } catch (e: Exception) {
                val errorMsg = e.message
                _uiState.value = _uiState.value.copy(
                    error = if (errorMsg != null) {
                        getApplication<Application>().getString(R.string.error_chain_run, errorMsg)
                    } else {
                        getApplication<Application>().getString(R.string.error_chain_run_unknown)
                    }
                )
            } finally {
                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    runningChainId = null
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun deleteChain(id: String) {
        viewModelScope.launch {
            chainRepository.deleteChain(id)
        }
    }
}
