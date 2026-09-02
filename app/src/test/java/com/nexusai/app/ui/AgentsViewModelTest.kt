package com.nexusai.app.ui

import android.app.Application
import com.nexusai.data.common.AppDataManager
import com.nexusai.domain.model.AIAgent
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AgentsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockApplication = mockk<Application>(relaxed = true)
    private lateinit var appDataManager: AppDataManager
    private lateinit var viewModel: AgentsViewModel

    private val agentsFlow = MutableStateFlow<List<AIAgent>>(emptyList())

    private fun createAgent(
        id: String = "agent-1",
        name: String = "Test Agent",
        description: String = "A test agent",
        systemPrompt: String = "You are a test agent",
        isActive: Boolean = true
    ) = AIAgent(
        id = id,
        name = name,
        description = description,
        systemPrompt = systemPrompt,
        isActive = isActive
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        appDataManager = mockk(relaxed = true)
        every { appDataManager.agents } returns agentsFlow
        viewModel = AgentsViewModel(mockApplication, appDataManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty agents and default values`() = runTest {
        val state = viewModel.uiState.value
        assertTrue(state.agents.isEmpty())
        assertEquals("", state.newAgentName)
        assertEquals("", state.newAgentDescription)
        assertEquals("", state.newAgentPrompt)
        assertNull(state.error)
    }

    @Test
    fun `init loads agents from appDataManager`() = runTest {
        val agents = listOf(createAgent(id = "1", name = "Agent A"), createAgent(id = "2", name = "Agent B"))
        agentsFlow.value = agents
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.agents.size)
        assertEquals("Agent A", state.agents[0].name)
        assertEquals("Agent B", state.agents[1].name)
    }

    @Test
    fun `setNewAgentName updates state`() {
        viewModel.setNewAgentName("My Agent")
        assertEquals("My Agent", viewModel.uiState.value.newAgentName)
    }

    @Test
    fun `setNewAgentDescription updates state`() {
        viewModel.setNewAgentDescription("Description text")
        assertEquals("Description text", viewModel.uiState.value.newAgentDescription)
    }

    @Test
    fun `setNewAgentPrompt updates state`() {
        viewModel.setNewAgentPrompt("System prompt text")
        assertEquals("System prompt text", viewModel.uiState.value.newAgentPrompt)
    }

    @Test
    fun `createAgent adds agent and clears form fields`() = runTest {
        viewModel.setNewAgentName("New Agent")
        viewModel.setNewAgentDescription("New Description")
        viewModel.setNewAgentPrompt("New Prompt")

        viewModel.createAgent()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { appDataManager.addAgent(match {
            it.name == "New Agent" &&
            it.description == "New Description" &&
            it.systemPrompt == "New Prompt"
        }) }

        val state = viewModel.uiState.value
        assertEquals("", state.newAgentName)
        assertEquals("", state.newAgentDescription)
        assertEquals("", state.newAgentPrompt)
    }

    @Test
    fun `createAgent does nothing when name is blank`() = runTest {
        viewModel.setNewAgentName("  ")
        viewModel.setNewAgentPrompt("Some prompt")

        viewModel.createAgent()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { appDataManager.addAgent(any()) }
    }

    @Test
    fun `createAgent does nothing when prompt is blank`() = runTest {
        viewModel.setNewAgentName("Some name")
        viewModel.setNewAgentPrompt("  ")

        viewModel.createAgent()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { appDataManager.addAgent(any()) }
    }

    @Test
    fun `createAgent does nothing when name and prompt are empty`() = runTest {
        viewModel.createAgent()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { appDataManager.addAgent(any()) }
    }

    @Test
    fun `createAgent sets error on failure`() = runTest {
        viewModel.setNewAgentName("Agent")
        viewModel.setNewAgentPrompt("Prompt")
        coEvery { appDataManager.addAgent(any()) } throws RuntimeException("DB error")

        viewModel.createAgent()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Ошибка создания агента"))
    }

    @Test
    fun `toggleAgent activates inactive agent`() = runTest {
        val agent = createAgent(id = "agent-1", isActive = false)
        agentsFlow.value = listOf(agent)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleAgent("agent-1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { appDataManager.updateAgent(match {
            it.id == "agent-1" && it.isActive
        }) }
    }

    @Test
    fun `toggleAgent deactivates active agent`() = runTest {
        val agent = createAgent(id = "agent-1", isActive = true)
        agentsFlow.value = listOf(agent)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleAgent("agent-1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { appDataManager.updateAgent(match {
            it.id == "agent-1" && !it.isActive
        }) }
    }

    @Test
    fun `toggleAgent does nothing for nonexistent agent`() = runTest {
        agentsFlow.value = emptyList()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleAgent("nonexistent")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { appDataManager.updateAgent(any()) }
    }

    @Test
    fun `toggleAgent sets error on failure`() = runTest {
        val agent = createAgent(id = "agent-1", isActive = true)
        agentsFlow.value = listOf(agent)
        testDispatcher.scheduler.advanceUntilIdle()
        coEvery { appDataManager.updateAgent(any()) } throws RuntimeException("Update error")

        viewModel.toggleAgent("agent-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Ошибка обновления агента"))
    }

    @Test
    fun `deleteAgent calls removeAgent on appDataManager`() = runTest {
        viewModel.deleteAgent("agent-1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { appDataManager.removeAgent("agent-1") }
    }

    @Test
    fun `deleteAgent sets error on failure`() = runTest {
        coEvery { appDataManager.removeAgent(any()) } throws RuntimeException("Delete error")

        viewModel.deleteAgent("agent-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Ошибка удаления агента"))
    }

    @Test
    fun `clearError resets error to null`() = runTest {
        coEvery { appDataManager.addAgent(any()) } throws RuntimeException("error")
        viewModel.setNewAgentName("Agent")
        viewModel.setNewAgentPrompt("Prompt")
        viewModel.createAgent()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `agents flow updates are reflected in state`() = runTest {
        assertTrue(viewModel.uiState.value.agents.isEmpty())

        agentsFlow.value = listOf(createAgent(id = "1", name = "First"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.agents.size)

        agentsFlow.value = listOf(
            createAgent(id = "1", name = "First"),
            createAgent(id = "2", name = "Second")
        )
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.agents.size)
    }

}
