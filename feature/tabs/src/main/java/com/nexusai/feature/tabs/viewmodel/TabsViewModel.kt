package com.nexusai.feature.tabs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.Message
import com.nexusai.domain.model.MessageRole
import com.nexusai.domain.model.Tab
import com.nexusai.domain.repository.AIProviderRepository
import com.nexusai.domain.repository.TabRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TabsUiState(
    val tabs: List<Tab> = emptyList(),
    val activeTabId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isGenerating: Boolean = false,
    val currentProvider: AIProviderConfig? = null,
    val inputText: String = ""
)

@HiltViewModel
class TabsViewModel @Inject constructor(
    private val tabRepository: TabRepository,
    private val aiProviderRepository: AIProviderRepository
) : ViewModel() {

    private val _tabsState = MutableStateFlow(TabsUiState())
    val tabsState: StateFlow<TabsUiState> = _tabsState.asStateFlow()

    private val _chatStates = MutableStateFlow<Map<String, ChatUiState>>(emptyMap())
    val chatStates: StateFlow<Map<String, ChatUiState>> = _chatStates.asStateFlow()

    private val _providers = MutableStateFlow<List<AIProviderConfig>>(emptyList())
    val providers: StateFlow<List<AIProviderConfig>> = _providers.asStateFlow()

    init {
        viewModelScope.launch {
            tabRepository.getAllTabs().collect { tabs ->
                _tabsState.value = _tabsState.value.copy(
                    tabs = tabs,
                    activeTabId = tabs.firstOrNull { it.isActive }?.id
                )
            }
        }
        viewModelScope.launch {
            aiProviderRepository.getAllProviders().collect { providers ->
                _providers.value = providers
            }
        }
    }

    fun createTab(title: String = "New Tab") {
        viewModelScope.launch {
            val tab = Tab(
                id = UUID.randomUUID().toString(),
                title = title
            )
            tabRepository.createTab(tab)
            tabRepository.setActiveTab(tab.id)
        }
    }

    fun deleteTab(id: String) {
        viewModelScope.launch {
            tabRepository.deleteTab(id)
        }
    }

    fun setActiveTab(id: String) {
        viewModelScope.launch {
            tabRepository.setActiveTab(id)
        }
    }

    fun renameTab(id: String, newTitle: String) {
        viewModelScope.launch {
            val tab = tabRepository.getTabById(id) ?: return@launch
            tabRepository.updateTab(tab.copy(title = newTitle))
        }
    }

    fun duplicateTab(id: String) {
        viewModelScope.launch {
            val original = tabRepository.getTabById(id) ?: return@launch
            val newTab = original.copy(
                id = UUID.randomUUID().toString(),
                title = "${original.title} (copy)",
                isActive = false,
                createdAt = System.currentTimeMillis()
            )
            tabRepository.createTab(newTab)
        }
    }

    fun setTabProvider(tabId: String, providerId: String) {
        viewModelScope.launch {
            val tab = tabRepository.getTabById(tabId) ?: return@launch
            val provider = _providers.value.firstOrNull { it.id == providerId }
            tabRepository.updateTab(tab.copy(aiProviderId = providerId))
            updateChatState(tabId) { it.copy(currentProvider = provider) }
        }
    }

    fun updateInput(tabId: String, text: String) {
        updateChatState(tabId) { it.copy(inputText = text) }
    }

    fun sendMessage(tabId: String) {
        val chatState = _chatStates.value[tabId] ?: return
        val text = chatState.inputText.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            val userMessage = Message(
                id = UUID.randomUUID().toString(),
                content = text,
                role = MessageRole.USER
            )

            updateChatState(tabId) {
                it.copy(
                    messages = it.messages + userMessage,
                    inputText = "",
                    isGenerating = true
                )
            }

            val provider = chatState.currentProvider
            if (provider == null) {
                val errorMessage = Message(
                    id = UUID.randomUUID().toString(),
                    content = "No AI provider selected. Please select a provider first.",
                    role = MessageRole.ASSISTANT
                )
                updateChatState(tabId) {
                    it.copy(
                        messages = it.messages + errorMessage,
                        isGenerating = false
                    )
                }
                return@launch
            }

            try {
                val response = Message(
                    id = UUID.randomUUID().toString(),
                    content = "[${provider.name}] Response will be implemented in Phase 3 with API integration",
                    role = MessageRole.ASSISTANT
                )
                updateChatState(tabId) {
                    it.copy(
                        messages = it.messages + response,
                        isGenerating = false
                    )
                }
            } catch (e: Exception) {
                val errorMessage = Message(
                    id = UUID.randomUUID().toString(),
                    content = "Error: ${e.message}",
                    role = MessageRole.ASSISTANT
                )
                updateChatState(tabId) {
                    it.copy(
                        messages = it.messages + errorMessage,
                        isGenerating = false
                    )
                }
            }
        }
    }

    fun stopGeneration(tabId: String) {
        updateChatState(tabId) { it.copy(isGenerating = false) }
    }

    private fun updateChatState(tabId: String, transform: (ChatUiState) -> ChatUiState) {
        _chatStates.value = _chatStates.value.toMutableMap().apply {
            this[tabId] = transform(this[tabId] ?: ChatUiState())
        }
    }
}
