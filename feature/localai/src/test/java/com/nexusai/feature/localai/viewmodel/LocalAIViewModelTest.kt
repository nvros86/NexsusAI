package com.nexusai.feature.localai.viewmodel

import com.nexusai.feature.localai.LocalAIConfig
import com.nexusai.feature.localai.LocalAIModel
import com.nexusai.feature.localai.LocalAIService
import com.nexusai.feature.localai.LocalAIType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class LocalAIViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var localAIService: LocalAIService
    private lateinit var viewModel: LocalAIViewModel

    private fun createConfig(
        id: String = "config-1",
        name: String = "Ollama",
        type: LocalAIType = LocalAIType.OLLAMA,
        baseUrl: String = "http://localhost:11434",
        isConnected: Boolean = false
    ) = LocalAIConfig(
        id = id,
        name = name,
        type = type,
        baseUrl = baseUrl,
        isConnected = isConnected
    )

    private fun createModel(
        id: String = "model-1",
        name: String = "llama3",
        size: Long = 4000000000
    ) = LocalAIModel(
        id = id,
        name = name,
        size = size
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        localAIService = mockk(relaxed = true)
        coEvery { localAIService.checkConnection(any()) } returns false
        viewModel = LocalAIViewModel(localAIService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state after init has default config`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.configs.size)
        assertEquals("Ollama", state.configs[0].name)
        assertTrue(state.models.isEmpty())
        assertNotNull(state.status)
        assertNull(state.selectedConfig)
        assertFalse(state.isGenerating)
        assertNull(state.lastResponse)
    }

    @Test
    fun `init loads default config`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.configs.size)
        assertEquals("Ollama", state.configs[0].name)
        assertEquals(LocalAIType.OLLAMA, state.configs[0].type)
        assertEquals("http://localhost:11434", state.configs[0].baseUrl)
    }

    @Test
    fun `addConfig adds config to list`() = runTest {
        viewModel.addConfig("Custom LLM", LocalAIType.LLAMACPP, "http://localhost:8080")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.configs.size >= 2)
        val added = state.configs.last()
        assertEquals("Custom LLM", added.name)
        assertEquals(LocalAIType.LLAMACPP, added.type)
        assertEquals("http://localhost:8080", added.baseUrl)
    }

    @Test
    fun `addConfig triggers connection test`() = runTest {
        coEvery { localAIService.checkConnection("http://localhost:8080") } returns true
        coEvery { localAIService.getModels("http://localhost:8080") } returns emptyList()

        viewModel.addConfig("Test", LocalAIType.OLLAMA, "http://localhost:8080")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { localAIService.checkConnection("http://localhost:8080") }
    }

    @Test
    fun `deleteConfig removes config from list`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val initialSize = viewModel.uiState.value.configs.size

        viewModel.addConfig("Extra", LocalAIType.OLLAMA, "http://localhost:11435")
        testDispatcher.scheduler.advanceUntilIdle()

        val addedConfig = viewModel.uiState.value.configs.last()
        viewModel.deleteConfig(addedConfig.id)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(initialSize, viewModel.uiState.value.configs.size)
    }

    @Test
    fun `deleteConfig does nothing for nonexistent id`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val sizeBefore = viewModel.uiState.value.configs.size

        viewModel.deleteConfig("nonexistent-id")
        assertEquals(sizeBefore, viewModel.uiState.value.configs.size)
    }

    @Test
    fun `testConnection success updates config isConnected`() = runTest {
        coEvery { localAIService.checkConnection("http://localhost:11434") } returns true
        coEvery { localAIService.getModels("http://localhost:11434") } returns listOf(createModel())

        viewModel.testConnection(createConfig())
        testDispatcher.scheduler.advanceUntilIdle()

        val config = viewModel.uiState.value.configs.first()
        assertTrue(config.isConnected)
    }

    @Test
    fun `testConnection success loads models and updates status`() = runTest {
        val models = listOf(createModel(id = "m1", name = "llama3"), createModel(id = "m2", name = "codellama"))
        coEvery { localAIService.checkConnection("http://localhost:11434") } returns true
        coEvery { localAIService.getModels("http://localhost:11434") } returns models

        viewModel.testConnection(createConfig())
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.models.size)
        assertTrue(state.status.isRunning)
        assertEquals(2, state.status.modelsCount)
    }

    @Test
    fun `testConnection failure sets status error`() = runTest {
        coEvery { localAIService.checkConnection("http://localhost:11434") } returns false

        viewModel.testConnection(createConfig())
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.status.isRunning)
        assertNotNull(state.status.error)
    }

    @Test
    fun `testConnection exception sets error`() = runTest {
        coEvery { localAIService.checkConnection(any()) } throws RuntimeException("Connection refused")

        viewModel.testConnection(createConfig())
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Connection refused"))
    }

    @Test
    fun `selectConfig updates selectedConfig`() = runTest {
        val config = createConfig()
        viewModel.selectConfig(config)

        assertEquals(config, viewModel.uiState.value.selectedConfig)
    }

    @Test
    fun `sendPrompt generates response when config is selected`() = runTest {
        coEvery { localAIService.checkConnection(any()) } returns true
        coEvery { localAIService.getModels(any()) } returns listOf(createModel())
        viewModel.testConnection(createConfig())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectConfig(viewModel.uiState.value.configs.first())
        coEvery { localAIService.generate(any(), "llama3", "Hello") } returns "Hi there!"

        viewModel.sendPrompt("llama3", "Hello")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Hi there!", state.lastResponse)
        assertFalse(state.isGenerating)
    }

    @Test
    fun `sendPrompt does nothing when no config selected`() = runTest {
        viewModel.sendPrompt("model", "Hello")
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.lastResponse)
    }

    @Test
    fun `sendPrompt sets error on failure`() = runTest {
        coEvery { localAIService.checkConnection(any()) } returns true
        coEvery { localAIService.getModels(any()) } returns listOf(createModel())
        viewModel.testConnection(createConfig())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectConfig(viewModel.uiState.value.configs.first())
        coEvery { localAIService.generate(any(), any(), any()) } throws RuntimeException("Timeout")

        viewModel.sendPrompt("model", "test")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.lastResponse)
        assertTrue(state.lastResponse!!.contains("Timeout"))
        assertFalse(state.isGenerating)
    }

    @Test
    fun `clearResponse resets lastResponse to null`() = runTest {
        coEvery { localAIService.checkConnection(any()) } returns true
        coEvery { localAIService.getModels(any()) } returns listOf(createModel())
        viewModel.testConnection(createConfig())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectConfig(viewModel.uiState.value.configs.first())
        coEvery { localAIService.generate(any(), any(), any()) } returns "result"
        viewModel.sendPrompt("model", "test")
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.lastResponse)

        viewModel.clearResponse()
        assertNull(viewModel.uiState.value.lastResponse)
    }

    @Test
    fun `clearError resets error to null`() = runTest {
        coEvery { localAIService.checkConnection(any()) } throws RuntimeException("error")
        viewModel.testConnection(createConfig())
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `refresh calls testConnection for all configs`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val config = viewModel.uiState.value.configs.first()

        coEvery { localAIService.checkConnection(config.baseUrl) } returns true
        coEvery { localAIService.getModels(config.baseUrl) } returns emptyList()

        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { localAIService.checkConnection(config.baseUrl) }
    }
}
