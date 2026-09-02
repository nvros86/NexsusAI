package com.nexusai.feature.localai

import io.ktor.client.HttpClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocalAIServiceTest {

    private lateinit var service: LocalAIService
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
        service = LocalAIService(httpClient = io.mockk.mockk(relaxed = true))
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `LocalAIType has correct default ports`() {
        assertEquals(11434, LocalAIType.OLLAMA.defaultPort)
        assertEquals(8080, LocalAIType.LLAMACPP.defaultPort)
        assertEquals(1234, LocalAIType.LM_STUDIO.defaultPort)
        assertEquals(8080, LocalAIType.LOCALAI.defaultPort)
        assertEquals(8080, LocalAIType.CUSTOM.defaultPort)
    }

    @Test
    fun `LocalAIType has correct display names`() {
        assertEquals("Ollama", LocalAIType.OLLAMA.displayName)
        assertEquals("llama.cpp", LocalAIType.LLAMACPP.displayName)
        assertEquals("LM Studio", LocalAIType.LM_STUDIO.displayName)
        assertEquals("LocalAI", LocalAIType.LOCALAI.displayName)
        assertEquals("Custom", LocalAIType.CUSTOM.displayName)
    }

    @Test
    fun `LocalAIConfig default values`() {
        val config = LocalAIConfig(
            id = "test",
            name = "Test",
            type = LocalAIType.OLLAMA
        )
        assertEquals("http://localhost:11434", config.baseUrl)
        assertFalse(config.isConnected)
        assertTrue(config.availableModels.isEmpty())
    }

    @Test
    fun `LocalAIModel default values`() {
        val model = LocalAIModel(
            id = "test",
            name = "test-model"
        )
        assertEquals(0L, model.size)
        assertEquals("", model.parameterSize)
        assertEquals("", model.quantization)
        assertFalse(model.isLoaded)
        assertEquals(0L, model.vramUsage)
    }

    @Test
    fun `LocalAIStatus default values`() {
        val status = LocalAIStatus()
        assertFalse(status.isRunning)
        assertEquals(LocalAIType.OLLAMA, status.serverType)
        assertEquals("", status.url)
        assertEquals(0, status.modelsCount)
        assertNull(status.loadedModel)
        assertEquals(0L, status.uptime)
        assertNull(status.error)
    }

    @Test
    fun `checkConnection returns false for invalid url`() = runTest {
        val result = service.checkConnection("http://localhost:99999")
        assertFalse(result)
    }

    @Test
    fun `getModels returns empty for invalid url`() = runTest {
        val models = service.getModels("http://localhost:99999")
        assertTrue(models.isEmpty())
    }

    @Test
    fun `generate returns error for invalid url`() = runTest {
        val result = service.generate(
            baseUrl = "http://localhost:99999",
            model = "test",
            prompt = "test"
        )
        assertTrue(result.startsWith("Error:"))
    }
}
