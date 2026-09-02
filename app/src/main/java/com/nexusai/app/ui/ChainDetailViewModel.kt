package com.nexusai.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.nexusai.app.R
import com.nexusai.domain.model.AutomationChain
import com.nexusai.domain.model.ChainRunResult
import com.nexusai.domain.model.ChainStep
import com.nexusai.domain.model.ChainStepType
import com.nexusai.domain.repository.ChainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChainDetailViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val chainRepository: ChainRepository
) : AndroidViewModel(application) {

    private val chainId: String = savedStateHandle["chainId"] ?: "new"

    private val _uiState = MutableStateFlow(ChainDetailUiState())
    val uiState: StateFlow<ChainDetailUiState> = _uiState.asStateFlow()

    init {
        if (chainId != "new") {
            loadChain()
        }
    }

    private fun loadChain() {
        viewModelScope.launch {
            val chain = chainRepository.getChainById(chainId)
            if (chain != null) {
                _uiState.value = _uiState.value.copy(
                    chainName = chain.name,
                    chainDescription = chain.description,
                    steps = chain.steps
                )
            }
        }
    }

    fun setChainName(name: String) {
        _uiState.value = _uiState.value.copy(chainName = name)
    }

    fun setChainDescription(description: String) {
        _uiState.value = _uiState.value.copy(chainDescription = description)
    }

    fun setNewStepName(name: String) {
        _uiState.value = _uiState.value.copy(newStepName = name)
    }

    fun setNewStepPrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(newStepPrompt = prompt)
    }

    fun setNewStepType(type: ChainStepType) {
        _uiState.value = _uiState.value.copy(newStepType = type)
    }

    fun addStep() {
        val state = _uiState.value
        if (state.newStepName.isBlank() || state.newStepPrompt.isBlank()) return

        val step = ChainStep(
            id = System.currentTimeMillis().toString(),
            type = state.newStepType,
            name = state.newStepName,
            prompt = state.newStepPrompt,
            outputKey = "step_${state.steps.size + 1}"
        )

        _uiState.value = state.copy(
            steps = state.steps + step,
            newStepName = "",
            newStepPrompt = ""
        )
        saveCurrentChain()
    }

    fun toggleStep(id: String) {
        _uiState.value = _uiState.value.copy(
            steps = _uiState.value.steps.map {
                if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it
            }
        )
        saveCurrentChain()
    }

    fun deleteStep(id: String) {
        _uiState.value = _uiState.value.copy(
            steps = _uiState.value.steps.filter { it.id != id }
        )
        saveCurrentChain()
    }

    fun runChain() {
        val state = _uiState.value
        if (state.steps.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = state.copy(isRunning = true)

            val chain = AutomationChain(
                id = chainId,
                name = state.chainName.ifEmpty { getApplication<Application>().getString(R.string.chain_default_name) },
                description = state.chainDescription,
                steps = state.steps
            )

            try {
                val result = chainRepository.runChain(chain)
                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    lastResult = result
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    lastResult = ChainRunResult(
                        chainId = chainId,
                        stepResults = emptyList(),
                        isError = true,
                        errorMessage = e.message
                    )
                )
            }
        }
    }

    private fun saveCurrentChain() {
        viewModelScope.launch {
            val state = _uiState.value
            val chain = AutomationChain(
                id = chainId,
                name = state.chainName.ifEmpty { getApplication<Application>().getString(R.string.chain_default_name) },
                description = state.chainDescription,
                steps = state.steps
            )
            chainRepository.saveChain(chain)
        }
    }
}
