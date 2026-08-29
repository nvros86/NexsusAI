package com.nexusai.feature.tabs.viewmodel

import com.nexusai.data.ai.AIProviderManager
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.Tab
import com.nexusai.domain.repository.AIProviderRepository
import com.nexusai.domain.repository.TabRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TabsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var tabRepository: TabRepository
    private lateinit var aiProviderRepository: AIProviderRepository
    private lateinit var aiProviderManager: AIProviderManager
    private lateinit var viewModel: TabsViewModel

    private val tabsFlow = MutableStateFlow<List<Tab>>(emptyList())
    private val providersFlow = MutableStateFlow<List<AIProviderConfig>>(emptyList())

    private fun createTab(
        id: String = "tab-1",
        title: String = "Test Tab",
        isActive: Boolean = false
    ) = Tab(id = id, title = title, isActive = isActive)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        tabRepository = mockk(relaxed = true)
        aiProviderRepository = mockk(relaxed = true)
        aiProviderManager = mockk(relaxed = true)

        every { tabRepository.getAllTabs() } returns tabsFlow
        every { aiProviderRepository.getAllProviders() } returns providersFlow
        coEvery { tabRepository.createTab(any()) } returns createTab()
        coEvery { tabRepository.getTabById(any()) } returns createTab()

        viewModel = TabsViewModel(tabRepository, aiProviderRepository, aiProviderManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty`() = runTest {
        val state = viewModel.tabsState.value
        assertTrue(state.tabs.isEmpty())
        assertNull(state.activeTabId)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `createTab calls repository`() = runTest {
        viewModel.createTab("My Tab")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { tabRepository.createTab(any()) }
        coVerify { tabRepository.setActiveTab(any()) }
    }

    @Test
    fun `deleteTab calls repository`() = runTest {
        viewModel.deleteTab("tab-1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { tabRepository.deleteTab("tab-1") }
    }

    @Test
    fun `setActiveTab calls repository`() = runTest {
        viewModel.setActiveTab("tab-2")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { tabRepository.setActiveTab("tab-2") }
    }

    @Test
    fun `renameTab updates tab title`() = runTest {
        val tab = createTab(id = "tab-1", title = "Old Title")
        coEvery { tabRepository.getTabById("tab-1") } returns tab

        viewModel.renameTab("tab-1", "New Title")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { tabRepository.updateTab(tab.copy(title = "New Title")) }
    }

    @Test
    fun `duplicateTab creates copy with copy suffix`() = runTest {
        val tab = createTab(id = "tab-1", title = "Original")
        coEvery { tabRepository.getTabById("tab-1") } returns tab

        viewModel.duplicateTab("tab-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val tabSlot = io.mockk.slot<Tab>()
        coVerify { tabRepository.createTab(capture(tabSlot)) }
        assertEquals("Original (copy)", tabSlot.captured.title)
        assertEquals(false, tabSlot.captured.isActive)
    }

    @Test
    fun `updateInput updates chat state inputText`() {
        viewModel.updateInput("tab-1", "Hello")
        val chatState = viewModel.chatStates.value["tab-1"]
        assertEquals("Hello", chatState?.inputText)
    }

    @Test
    fun `addAttachment adds to pending list`() {
        val file = com.nexusai.domain.model.AttachedFile(
            id = "f1", name = "test.txt", uri = "content://...", mimeType = "text/plain", size = 100
        )
        viewModel.addAttachment("tab-1", file)
        val chatState = viewModel.chatStates.value["tab-1"]
        assertEquals(1, chatState?.pendingAttachments?.size)
    }

    @Test
    fun `removeAttachment removes from pending list`() {
        val file = com.nexusai.domain.model.AttachedFile(
            id = "f1", name = "test.txt", uri = "content://...", mimeType = "text/plain", size = 100
        )
        viewModel.addAttachment("tab-1", file)
        viewModel.removeAttachment("tab-1", "f1")
        val chatState = viewModel.chatStates.value["tab-1"]
        assertTrue(chatState?.pendingAttachments?.isEmpty() == true)
    }

    @Test
    fun `sendMessage with empty text and no attachments does nothing`() {
        viewModel.sendMessage("tab-1")
        val chatState = viewModel.chatStates.value["tab-1"]
        assertEquals(0, chatState?.messages?.size ?: 0)
        assertFalse(chatState?.isGenerating == true)
    }

    @Test
    fun `stopGeneration sets isGenerating false`() {
        viewModel.updateInput("tab-1", "Hello")
        viewModel.sendMessage("tab-1")
        viewModel.stopGeneration("tab-1")
        val chatState = viewModel.chatStates.value["tab-1"]
        assertFalse(chatState?.isGenerating == true)
    }

    @Test
    fun `sendMessage without provider shows error message`() = runTest {
        viewModel.updateInput("tab-1", "Hello")
        viewModel.sendMessage("tab-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val chatState = viewModel.chatStates.value["tab-1"]
        val messages = chatState?.messages
        assertTrue(messages?.any { it.content.contains("No AI provider selected") } == true)
    }
}
