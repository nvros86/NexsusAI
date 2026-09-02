package com.nexusai.feature.tabs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.data.ai.AIProviderManager
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.AttachedFile
import com.nexusai.domain.model.Message
import com.nexusai.domain.model.MessageRole
import com.nexusai.domain.model.Tab
import com.nexusai.domain.repository.AIProviderRepository
import com.nexusai.domain.repository.TabRepository
import com.nexusai.data.common.AppDataManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TabsUiState(
    val tabs: List<Tab> = emptyList(),
    val activeTabId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSplitViewMode: Boolean = false,
    val splitViewTabIds: Pair<String, String>? = null,
    val searchQuery: String = "",
    val searchResults: List<Tab> = emptyList(),
    val isSearchActive: Boolean = false
)

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isGenerating: Boolean = false,
    val currentProvider: AIProviderConfig? = null,
    val inputText: String = "",
    val pendingAttachments: List<AttachedFile> = emptyList()
)

@HiltViewModel
class TabsViewModel @Inject constructor(
    private val tabRepository: TabRepository,
    private val aiProviderRepository: AIProviderRepository,
    private val aiProviderManager: AIProviderManager,
    private val appDataManager: AppDataManager
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

    fun closeAllTabs() {
        viewModelScope.launch {
            val tabs = _tabsState.value.tabs
            tabs.forEach { tab ->
                tabRepository.deleteTab(tab.id)
            }
            // Create a new default tab after closing all
            createTab("New Tab")
        }
    }

    fun setTabProvider(tabId: String, providerId: String) {
        viewModelScope.launch {
            val provider = _providers.value.firstOrNull { it.id == providerId } ?: return@launch
            val tab = tabRepository.getTabById(tabId) ?: return@launch
            tabRepository.updateTab(tab.copy(aiProviderId = providerId))
            updateChatState(tabId) { it.copy(currentProvider = provider) }
        }
    }

    fun setTabAgent(tabId: String, agentId: String?) {
        viewModelScope.launch {
            val tab = tabRepository.getTabById(tabId) ?: return@launch
            tabRepository.updateTab(tab.copy(agentId = agentId))
        }
    }

    fun updateInput(tabId: String, text: String) {
        updateChatState(tabId) { it.copy(inputText = text) }
    }

    fun addAttachment(tabId: String, file: AttachedFile) {
        updateChatState(tabId) {
            it.copy(pendingAttachments = it.pendingAttachments + file)
        }
    }

    fun removeAttachment(tabId: String, fileId: String) {
        updateChatState(tabId) {
            it.copy(pendingAttachments = it.pendingAttachments.filter { f -> f.id != fileId })
        }
    }

    fun sendMessage(tabId: String) {
        val chatState = _chatStates.value[tabId] ?: return
        val text = chatState.inputText.trim()
        val attachments = chatState.pendingAttachments
        if (text.isEmpty() && attachments.isEmpty()) return

        viewModelScope.launch {
            val userMessage = Message(
                id = UUID.randomUUID().toString(),
                content = text.ifEmpty { "Attached file(s)" },
                role = MessageRole.USER,
                attachments = attachments
            )

            updateChatState(tabId) {
                it.copy(
                    messages = it.messages + userMessage,
                    inputText = "",
                    pendingAttachments = emptyList(),
                    isGenerating = true
                )
            }

            val providerConfig = chatState.currentProvider
            if (providerConfig == null) {
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
                val provider = aiProviderManager.getProvider(providerConfig)
                val tab = tabRepository.getTabById(tabId)

                val systemPromptParts = mutableListOf<String>()

                val agentPrompt = appDataManager.getActiveAgentSystemPrompt()
                if (agentPrompt != null) {
                    systemPromptParts.add("System Instructions:\n$agentPrompt")
                }

                val memoryContext = appDataManager.getMemoryContext()
                if (memoryContext != null) {
                    systemPromptParts.add("User Context (from memory):\n$memoryContext")
                }

                val providerSystemPrompt = providerConfig.systemPrompt
                if (providerSystemPrompt.isNotBlank()) {
                    systemPromptParts.add(providerSystemPrompt)
                }

                val fullSystemPrompt = systemPromptParts.joinToString("\n\n")

                val chatMessages = mutableListOf<com.nexusai.domain.ai.ChatMessage>()

                if (fullSystemPrompt.isNotBlank()) {
                    chatMessages.add(
                        com.nexusai.domain.ai.ChatMessage(
                            role = com.nexusai.domain.ai.MessageRole.SYSTEM,
                            content = fullSystemPrompt
                        )
                    )
                }

                chatState.messages.map {
                    com.nexusai.domain.ai.ChatMessage(
                        role = when (it.role) {
                            MessageRole.USER -> com.nexusai.domain.ai.MessageRole.USER
                            MessageRole.ASSISTANT -> com.nexusai.domain.ai.MessageRole.ASSISTANT
                            MessageRole.SYSTEM -> com.nexusai.domain.ai.MessageRole.SYSTEM
                        },
                        content = it.content
                    )
                }.forEach { chatMessages.add(it) }

                chatMessages.add(
                    com.nexusai.domain.ai.ChatMessage(
                        role = com.nexusai.domain.ai.MessageRole.USER,
                        content = text.ifEmpty { "Attached file(s)" }
                    )
                )

                val assistantMessageId = UUID.randomUUID().toString()
                val streamingMessage = Message(
                    id = assistantMessageId,
                    content = "",
                    role = MessageRole.ASSISTANT
                )

                updateChatState(tabId) {
                    it.copy(
                        messages = it.messages + streamingMessage,
                        isGenerating = true
                    )
                }

                var fullResponse = ""
                provider.sendMessageStream(
                    messages = chatMessages,
                    model = providerConfig.defaultModel,
                    maxTokens = providerConfig.maxTokens,
                    temperature = providerConfig.temperature
                ).collect { token ->
                    fullResponse += token
                    updateChatState(tabId) { state ->
                        val updatedMsgs = state.messages.map { msg ->
                            if (msg.id == assistantMessageId) msg.copy(content = fullResponse) else msg
                        }
                        state.copy(messages = updatedMsgs)
                    }
                }

                val assistantMessage = Message(
                    id = assistantMessageId,
                    content = fullResponse,
                    role = MessageRole.ASSISTANT
                )

                updateChatState(tabId) {
                    it.copy(
                        isGenerating = false
                    )
                }

                // Save messages to database
                val currentTab = tabRepository.getTabById(tabId)
                if (currentTab != null) {
                    val finalMessages = currentTab.messages.map { msg ->
                        if (msg.id == assistantMessageId) assistantMessage else msg
                    }
                    tabRepository.updateTab(currentTab.copy(messages = finalMessages))
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

                // Save messages to database even on error
                val errorTab = tabRepository.getTabById(tabId)
                if (errorTab != null) {
                    val errorMessages = errorTab.messages + errorMessage
                    tabRepository.updateTab(errorTab.copy(messages = errorMessages))
                }
            }
        }
    }

    fun stopGeneration(tabId: String) {
        updateChatState(tabId) { it.copy(isGenerating = false) }
    }

    fun enableSplitView(leftTabId: String, rightTabId: String) {
        _tabsState.value = _tabsState.value.copy(
            isSplitViewMode = true,
            splitViewTabIds = Pair(leftTabId, rightTabId)
        )
    }

    fun disableSplitView() {
        _tabsState.value = _tabsState.value.copy(
            isSplitViewMode = false,
            splitViewTabIds = null
        )
    }

    fun setSearchQuery(query: String) {
        _tabsState.value = _tabsState.value.copy(searchQuery = query)
        if (query.isBlank()) {
            _tabsState.value = _tabsState.value.copy(searchResults = emptyList(), isSearchActive = false)
        } else {
            viewModelScope.launch {
                tabRepository.searchTabs(query).collect { results ->
                    _tabsState.value = _tabsState.value.copy(searchResults = results, isSearchActive = true)
                }
            }
        }
    }

    fun clearSearch() {
        _tabsState.value = _tabsState.value.copy(
            searchQuery = "",
            searchResults = emptyList(),
            isSearchActive = false
        )
    }

    private fun updateChatState(tabId: String, transform: (ChatUiState) -> ChatUiState) {
        _chatStates.value = _chatStates.value.toMutableMap().apply {
            this[tabId] = transform(this[tabId] ?: ChatUiState())
        }
    }
}
