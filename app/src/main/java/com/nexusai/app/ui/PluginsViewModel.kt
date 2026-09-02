package com.nexusai.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.app.R
import com.nexusai.domain.model.NexsusPlugin
import com.nexusai.domain.model.PluginCommand
import com.nexusai.domain.repository.PluginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PluginsViewModel @Inject constructor(
    application: Application,
    private val pluginRepository: PluginRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PluginsUiState())
    val uiState: StateFlow<PluginsUiState> = _uiState.asStateFlow()

    init {
        loadPlugins()
    }

    private fun loadPlugins() {
        viewModelScope.launch {
            pluginRepository.getAllPlugins().collect { plugins ->
                _uiState.value = _uiState.value.copy(plugins = plugins)
            }
        }
    }

    fun togglePlugin(id: String) {
        viewModelScope.launch {
            val plugin = _uiState.value.plugins.find { it.id == id }
            if (plugin != null) {
                if (plugin.isEnabled) {
                    pluginRepository.disablePlugin(id)
                } else {
                    pluginRepository.enablePlugin(id)
                }
            }
        }
    }

    fun selectPlugin(plugin: NexsusPlugin) {
        viewModelScope.launch {
            val commands = pluginRepository.getPluginCommands(plugin.id)
            _uiState.value = _uiState.value.copy(
                selectedPlugin = plugin,
                pluginCommands = commands
            )
        }
    }

    fun setExecuteArgs(args: String) {
        _uiState.value = _uiState.value.copy(executeArgs = args)
    }

    fun executeCommand(commandId: String) {
        val state = _uiState.value
        val plugin = state.selectedPlugin ?: return

        viewModelScope.launch {
            _uiState.value = state.copy(isExecuting = true)

            try {
                val result = pluginRepository.executeCommand(
                    pluginId = plugin.id,
                    commandId = commandId,
                    args = state.executeArgs
                )

                _uiState.value = _uiState.value.copy(
                    isExecuting = false,
                    lastResult = if (result.success) {
                        result.output
                    } else {
                        getApplication<Application>().getString(R.string.error_plugin_execute, result.error ?: "")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExecuting = false,
                    lastResult = getApplication<Application>().getString(R.string.error_plugin_execute, e.message ?: "")
                )
            }
        }
    }

    fun clearLastResult() {
        _uiState.value = _uiState.value.copy(lastResult = null)
    }
}
