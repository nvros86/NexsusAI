package com.nexusai.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.nexusai.domain.model.ModuleType
import com.nexusai.domain.model.NexusModule
import com.nexusai.domain.repository.ModuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

private val Context.moduleDataStore: DataStore<Preferences> by preferencesDataStore(name = "modules")

@Singleton
class ModuleRepositoryImpl @Inject constructor(
    private val context: Context
) : ModuleRepository {

    private val enabledOverrides = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    private val builtinModules = listOf(
        NexusModule(
            id = "marketplace",
            title = "AI Маркетплейс",
            description = "Каталог AI-провайдеров с одной кнопкой подключения",
            type = ModuleType.AI_PROVIDER,
            iconId = "ShoppingCart",
            route = "marketplace",
            capabilities = listOf("Подключение AI", "Пресеты", "Поиск")
        ),
        NexusModule(
            id = "playground",
            title = "Code Playground",
            description = "Редактор HTML/CSS/JS с live-превью в WebView",
            type = ModuleType.TOOL,
            iconId = "PhoneAndroid",
            route = "code_playground",
            capabilities = listOf("HTML", "CSS", "JavaScript", "Live Preview")
        ),
        NexusModule(
            id = "prompts",
            title = "Библиотека промптов",
            description = "16 готовых промптов с категориями и поиском",
            type = ModuleType.FEATURE,
            iconId = "Lightbulb",
            route = "prompts",
            capabilities = listOf("Поиск", "Категории", "Избранное", "Копирование")
        ),
        NexusModule(
            id = "templates",
            title = "Шаблоны задач",
            description = "Готовые шаблоны для типичных задач AI",
            type = ModuleType.FEATURE,
            iconId = "TextSnippet",
            route = "templates",
            capabilities = listOf("11 шаблонов", "7 категорий", "Поиск")
        ),
        NexusModule(
            id = "export",
            title = "Экспорт данных",
            description = "Экспорт чатов в Markdown, TXT, JSON, HTML",
            type = ModuleType.TOOL,
            iconId = "IosShare",
            route = "export",
            capabilities = listOf("Markdown", "TXT", "JSON", "HTML", "Share")
        ),
        NexusModule(
            id = "files",
            title = "Файловый менеджер",
            description = "Просмотр и управление файлами с фильтрацией",
            type = ModuleType.TOOL,
            iconId = "Description",
            route = "files",
            capabilities = listOf("Галерея", "Фильтры", "Share", "Delete")
        ),
        NexusModule(
            id = "code_editor",
            title = "Редактор кода",
            description = "Подсветка синтаксиса и редактирование кода",
            type = ModuleType.TOOL,
            iconId = "Code",
            route = "code",
            capabilities = listOf("Подсветка", "Строки", "Тулбар")
        ),
        NexusModule(
            id = "ai_chat",
            title = "AI Чат",
            description = "Основной чат с AI-провайдерами",
            type = ModuleType.AI_PROVIDER,
            iconId = "Psychology",
            isRequired = true,
            route = "chat",
            capabilities = listOf("Multi-provider", "Streaming", "История")
        ),
        NexusModule(
            id = "ai_router",
            title = "AI Router",
            description = "Авто-выбор лучшего AI для задачи с failover",
            type = ModuleType.AI_PROVIDER,
            iconId = "SwapHoriz",
            route = "ai_router",
            capabilities = listOf("Auto-select", "Failover", "Cost optimization")
        ),
        NexusModule(
            id = "split_view",
            title = "Split View",
            description = "Сравнение ответов 2-4 AI на один вопрос",
            type = ModuleType.FEATURE,
            iconId = "SmartToy",
            route = "split_view",
            capabilities = listOf("2-4 AI", "Сравнение", "Рейтинг")
        ),
        NexusModule(
            id = "voice_mode",
            title = "Voice Mode",
            description = "Голосовое общение с AI (STT → LLM → TTS)",
            type = ModuleType.FEATURE,
            iconId = "Mic",
            route = "voice_mode",
            capabilities = listOf("STT", "TTS", "Real-time")
        )
    )

    override fun getAllModules(): Flow<List<NexusModule>> {
        return flow {
            enabledOverrides.collect { overrides ->
                emit(builtinModules.map { m ->
                    val enabled = overrides[m.id] ?: m.isEnabled
                    m.copy(isEnabled = enabled)
                })
            }
        }
    }

    override fun getEnabledModules(): Flow<List<NexusModule>> {
        return flow {
            enabledOverrides.collect { overrides ->
                emit(builtinModules.filter { m ->
                    val enabled = overrides[m.id] ?: m.isEnabled
                    enabled
                }.map { m ->
                    val enabled = overrides[m.id] ?: m.isEnabled
                    m.copy(isEnabled = enabled)
                })
            }
        }
    }

    override fun getModulesByType(type: String): Flow<List<NexusModule>> {
        return flow {
            enabledOverrides.collect { overrides ->
                emit(builtinModules.filter { it.type.name == type }.map { m ->
                    val enabled = overrides[m.id] ?: m.isEnabled
                    m.copy(isEnabled = enabled)
                })
            }
        }
    }

    override fun searchModules(query: String): Flow<List<NexusModule>> {
        return flow {
            enabledOverrides.collect { overrides ->
                emit(builtinModules.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.description.contains(query, ignoreCase = true) ||
                            it.capabilities.any { cap -> cap.contains(query, ignoreCase = true) }
                }.map { m ->
                    val enabled = overrides[m.id] ?: m.isEnabled
                    m.copy(isEnabled = enabled)
                })
            }
        }
    }

    override suspend fun setModuleEnabled(id: String, enabled: Boolean) {
        enabledOverrides.value = enabledOverrides.value + (id to enabled)
        context.moduleDataStore.edit { prefs ->
            prefs[booleanPreferencesKey(id)] = enabled
        }
    }

    override suspend fun isModuleEnabled(id: String): Boolean {
        val overrides = enabledOverrides.value
        if (overrides.containsKey(id)) return overrides[id]!!

        val module = builtinModules.find { it.id == id } ?: return false
        return module.isEnabled
    }
}
