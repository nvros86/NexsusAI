package com.nexusai.app.ui

import android.app.Application
import com.nexusai.domain.model.NexsusPlugin
import com.nexusai.domain.model.PluginCapability
import com.nexusai.domain.model.PluginCommand
import com.nexusai.domain.model.PluginExecutionResult
import com.nexusai.domain.repository.PluginRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PluginsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: Application
    private lateinit var pluginRepository: PluginRepository
    private lateinit var viewModel: PluginsViewModel

    private val pluginsFlow = MutableStateFlow<List<NexsusPlugin>>(emptyList())

    private fun createPlugin(
        id: String = "plugin-1",
        name: String = "Test Plugin",
        description: String = "A test plugin",
        isEnabled: Boolean = true
    ) = NexsusPlugin(
        id = id,
        name = name,
        description = description,
        version = "1.0.0",
        author = "Test",
        iconEmoji = "🔌",
        capabilities = listOf(PluginCapability.CODE_EXECUTION),
        isEnabled = isEnabled
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = mockk(relaxed = true)
        every { application.getString(any<Int>()) } returns "Error"
        every { application.getString(any<Int>(), any()) } returns "Error: %s"
        pluginRepository = mockk(relaxed = true)
        every { pluginRepository.getAllPlugins() } returns pluginsFlow
        viewModel = PluginsViewModel(application, pluginRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        val state = viewModel.uiState.value
        assertTrue(state.plugins.isEmpty())
        assertNull(state.selectedPlugin)
        assertTrue(state.pluginCommands.isEmpty())
        assertFalse(state.isExecuting)
        assertNull(state.lastResult)
        assertEquals("", state.executeArgs)
    }

    @Test
    fun `init loads plugins from repository`() = runTest {
        val plugins = listOf(
            createPlugin(id = "1", name = "Plugin A"),
            createPlugin(id = "2", name = "Plugin B")
        )
        pluginsFlow.value = plugins
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.plugins.size)
        assertEquals("Plugin A", state.plugins[0].name)
        assertEquals("Plugin B", state.plugins[1].name)
    }

    @Test
    fun `togglePlugin enables disabled plugin`() = runTest {
        val plugin = createPlugin(id = "plugin-1", isEnabled = false)
        pluginsFlow.value = listOf(plugin)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.togglePlugin("plugin-1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { pluginRepository.enablePlugin("plugin-1") }
    }

    @Test
    fun `togglePlugin disables enabled plugin`() = runTest {
        val plugin = createPlugin(id = "plugin-1", isEnabled = true)
        pluginsFlow.value = listOf(plugin)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.togglePlugin("plugin-1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { pluginRepository.disablePlugin("plugin-1") }
    }

    @Test
    fun `togglePlugin does nothing for nonexistent plugin`() = runTest {
        pluginsFlow.value = emptyList()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.togglePlugin("nonexistent")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { pluginRepository.enablePlugin(any()) }
        coVerify(exactly = 0) { pluginRepository.disablePlugin(any()) }
    }

    @Test
    fun `selectPlugin loads commands and sets selectedPlugin`() = runTest {
        val plugin = createPlugin(id = "plugin-1")
        val commands = listOf(
            PluginCommand(id = "cmd-1", name = "Run", description = "Run plugin", usage = "run", pluginId = "plugin-1")
        )
        coEvery { pluginRepository.getPluginCommands("plugin-1") } returns commands

        viewModel.selectPlugin(plugin)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(plugin, state.selectedPlugin)
        assertEquals(1, state.pluginCommands.size)
        assertEquals("Run", state.pluginCommands[0].name)
    }

    @Test
    fun `setExecuteArgs updates state`() {
        viewModel.setExecuteArgs("--verbose --output=logs")
        assertEquals("--verbose --output=logs", viewModel.uiState.value.executeArgs)
    }

    @Test
    fun `executeCommand success sets lastResult to output`() = runTest {
        val plugin = createPlugin(id = "plugin-1")
        coEvery { pluginRepository.getPluginCommands("plugin-1") } returns emptyList()
        viewModel.selectPlugin(plugin)
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery {
            pluginRepository.executeCommand("plugin-1", "cmd-1", "")
        } returns PluginExecutionResult(
            pluginId = "plugin-1",
            commandId = "cmd-1",
            success = true,
            output = "Command executed successfully"
        )

        viewModel.executeCommand("cmd-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isExecuting)
        assertEquals("Command executed successfully", state.lastResult)
    }

    @Test
    fun `executeCommand failure sets lastResult to error message`() = runTest {
        val plugin = createPlugin(id = "plugin-1")
        coEvery { pluginRepository.getPluginCommands("plugin-1") } returns emptyList()
        viewModel.selectPlugin(plugin)
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery {
            pluginRepository.executeCommand("plugin-1", "cmd-1", "")
        } returns PluginExecutionResult(
            pluginId = "plugin-1",
            commandId = "cmd-1",
            success = false,
            output = "",
            error = "Something went wrong"
        )

        viewModel.executeCommand("cmd-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isExecuting)
        assertNotNull(state.lastResult)
    }

    @Test
    fun `executeCommand exception sets lastResult to error`() = runTest {
        val plugin = createPlugin(id = "plugin-1")
        coEvery { pluginRepository.getPluginCommands("plugin-1") } returns emptyList()
        viewModel.selectPlugin(plugin)
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery {
            pluginRepository.executeCommand("plugin-1", "cmd-1", "")
        } throws RuntimeException("Connection error")

        viewModel.executeCommand("cmd-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isExecuting)
        assertNotNull(state.lastResult)
    }

    @Test
    fun `executeCommand does nothing when no plugin selected`() = runTest {
        viewModel.executeCommand("cmd-1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { pluginRepository.executeCommand(any(), any(), any()) }
    }

    @Test
    fun `clearLastResult resets lastResult to null`() = runTest {
        val plugin = createPlugin(id = "plugin-1")
        coEvery { pluginRepository.getPluginCommands("plugin-1") } returns emptyList()
        viewModel.selectPlugin(plugin)
        testDispatcher.scheduler.advanceUntilIdle()
        coEvery {
            pluginRepository.executeCommand(any(), any(), any())
        } returns PluginExecutionResult(
            pluginId = "plugin-1",
            commandId = "cmd-1",
            success = true,
            output = "result"
        )
        viewModel.executeCommand("cmd-1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.lastResult)

        viewModel.clearLastResult()
        assertNull(viewModel.uiState.value.lastResult)
    }

    @Test
    fun `plugins flow updates are reflected in state`() = runTest {
        assertTrue(viewModel.uiState.value.plugins.isEmpty())

        pluginsFlow.value = listOf(createPlugin(id = "1", name = "First"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.plugins.size)

        pluginsFlow.value = listOf(
            createPlugin(id = "1", name = "First"),
            createPlugin(id = "2", name = "Second")
        )
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.plugins.size)
    }
}
