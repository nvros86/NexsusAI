package com.nexusai.domain.repository

import com.nexusai.domain.model.NexsusPlugin
import com.nexusai.domain.model.PluginCommand
import com.nexusai.domain.model.PluginExecutionResult
import kotlinx.coroutines.flow.Flow

interface PluginRepository {
    fun getAllPlugins(): Flow<List<NexsusPlugin>>
    fun getEnabledPlugins(): Flow<List<NexsusPlugin>>
    suspend fun getPluginById(id: String): NexsusPlugin?
    suspend fun enablePlugin(id: String)
    suspend fun disablePlugin(id: String)
    suspend fun installPlugin(plugin: NexsusPlugin)
    suspend fun uninstallPlugin(id: String)
    suspend fun executeCommand(pluginId: String, commandId: String, args: String): PluginExecutionResult
    suspend fun getPluginCommands(pluginId: String): List<PluginCommand>
}
