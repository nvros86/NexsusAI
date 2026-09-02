package com.nexusai.app.ui

import com.nexusai.data.ai.AIProviderManager
import com.nexusai.domain.ai.AIProvider
import com.nexusai.domain.ai.AIResponse
import com.nexusai.domain.ai.ChatMessage
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.ComparisonMode
import com.nexusai.domain.model.MessageRole
import com.nexusai.domain.model.SplitResult
import com.nexusai.domain.model.SplitSession
import com.nexusai.domain.repository.AIProviderRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
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
class SplitViewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var providerRepository: AIProviderRepository
    private lateinit var aiProviderManager: AIProviderManager
    private lateinit var viewModel: SplitViewViewModel

    private val providersFlow = MutableStateFlow<List<AIProviderConfig>>(emptyList())

    private fun createProvider(
        id: String = "provider-1",
        name: String = "OpenAI",
        apiKey: String = "sk-test",
        defaultModel: String = "gpt-4",
        models: List<String> = listOf("gpt-4", "gpt-3.5-turbo"),
        maxTokens: Int = 4096,
        temperature: Float = 0.7f
    ) = AIProviderConfig(
        id = id,
        name = name,
        type = com.nexusai.domain.model.ProviderType.OPENAI,
        baseUrl = "https://api.openai.com/v1",
        apiKey = apiKey,
        models = models,
        defaultModel = defaultModel,
        maxTokens = maxTokens,
        temperature = temperature
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        providerRepository = mockk(relaxed = true)
        aiProviderManager = mockk(relaxed = true)
        every { providerRepository.getAllProviders() } returns providersFlow
        viewModel = SplitViewViewModel(providerRepository, aiProviderManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        val state = viewModel.uiState.value
        assertEquals("", state.query)
        assertEquals(ComparisonMode.TWO, state.comparisonMode)
        assertTrue(state.selectedProviders.isEmpty())
        assertTrue(state.availableProviders.isEmpty())
        assertTrue(state.results.isEmpty())
        assertFalse(state.isRunning)
        assertTrue(state.sessions.isEmpty())
        assertNull(state.selectedSession)
    }

    @Test
    fun `init loads providers from repository`() = runTest {
        val providers = listOf(
            createProvider(id = "1", name = "OpenAI", apiKey = "sk-1"),
            createProvider(id = "2", name = "Anthropic", apiKey = "sk-2")
        )
        providersFlow.value = providers
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.availableProviders.size)
        assertEquals(2, state.selectedProviders.size)
    }

    @Test
    fun `init filters out providers without api key`() = runTest {
        val providers = listOf(
            createProvider(id = "1", name = "OpenAI", apiKey = "sk-1"),
            createProvider(id = "2", name = "Empty", apiKey = "")
        )
        providersFlow.value = providers
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.availableProviders.size)
        assertEquals("OpenAI", state.availableProviders[0].name)
    }

    @Test
    fun `setQuery updates query`() {
        viewModel.setQuery("Hello AI")
        assertEquals("Hello AI", viewModel.uiState.value.query)
    }

    @Test
    fun `setComparisonMode updates mode and selected providers`() = runTest {
        val providers = listOf(
            createProvider(id = "1", name = "P1", apiKey = "k1"),
            createProvider(id = "2", name = "P2", apiKey = "k2"),
            createProvider(id = "3", name = "P3", apiKey = "k3")
        )
        providersFlow.value = providers
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setComparisonMode(ComparisonMode.THREE)
        val state = viewModel.uiState.value

        assertEquals(ComparisonMode.THREE, state.comparisonMode)
        assertEquals(3, state.selectedProviders.size)
    }

    @Test
    fun `setComparisonMode to TWO limits selected providers`() = runTest {
        val providers = listOf(
            createProvider(id = "1", name = "P1", apiKey = "k1"),
            createProvider(id = "2", name = "P2", apiKey = "k2"),
            createProvider(id = "3", name = "P3", apiKey = "k3")
        )
        providersFlow.value = providers
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setComparisonMode(ComparisonMode.THREE)
        viewModel.setComparisonMode(ComparisonMode.TWO)
        val state = viewModel.uiState.value

        assertEquals(ComparisonMode.TWO, state.comparisonMode)
        assertEquals(2, state.selectedProviders.size)
    }

    @Test
    fun `toggleProvider adds provider when not selected and under limit`() = runTest {
        val providers = listOf(
            createProvider(id = "1", name = "P1", apiKey = "k1"),
            createProvider(id = "2", name = "P2", apiKey = "k2"),
            createProvider(id = "3", name = "P3", apiKey = "k3")
        )
        providersFlow.value = providers
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setComparisonMode(ComparisonMode.THREE)
        val provider3 = providers[2]
        viewModel.toggleProvider(provider3)

        val state = viewModel.uiState.value
        assertEquals(3, state.selectedProviders.size)
        assertTrue(state.selectedProviders.any { it.id == "3" })
    }

    @Test
    fun `toggleProvider does not add provider when at mode limit`() = runTest {
        val providers = listOf(
            createProvider(id = "1", name = "P1", apiKey = "k1"),
            createProvider(id = "2", name = "P2", apiKey = "k2"),
            createProvider(id = "3", name = "P3", apiKey = "k3")
        )
        providersFlow.value = providers
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setComparisonMode(ComparisonMode.TWO)
        val provider3 = providers[2]
        viewModel.toggleProvider(provider3)

        val state = viewModel.uiState.value
        assertEquals(2, state.selectedProviders.size)
        assertFalse(state.selectedProviders.any { it.id == "3" })
    }

    @Test
    fun `toggleProvider removes provider when selected`() = runTest {
        val providers = listOf(
            createProvider(id = "1", name = "P1", apiKey = "k1"),
            createProvider(id = "2", name = "P2", apiKey = "k2")
        )
        providersFlow.value = providers
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setComparisonMode(ComparisonMode.TWO)
        val provider1 = providers[0]
        viewModel.toggleProvider(provider1)

        val state = viewModel.uiState.value
        assertEquals(1, state.selectedProviders.size)
        assertFalse(state.selectedProviders.any { it.id == "1" })
    }

    @Test
    fun `toggleProvider does not remove last provider`() = runTest {
        val providers = listOf(
            createProvider(id = "1", name = "P1", apiKey = "k1")
        )
        providersFlow.value = providers
        testDispatcher.scheduler.advanceUntilIdle()

        val provider1 = providers[0]
        viewModel.toggleProvider(provider1)

        val state = viewModel.uiState.value
        assertEquals(1, state.selectedProviders.size)
    }

    @Test
    fun `toggleProvider does not exceed mode limit`() = runTest {
        val providers = listOf(
            createProvider(id = "1", name = "P1", apiKey = "k1"),
            createProvider(id = "2", name = "P2", apiKey = "k2"),
            createProvider(id = "3", name = "P3", apiKey = "k3")
        )
        providersFlow.value = providers
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setComparisonMode(ComparisonMode.TWO)
        viewModel.toggleProvider(providers[0])
        viewModel.toggleProvider(providers[1])

        val state = viewModel.uiState.value
        assertEquals(2, state.selectedProviders.size)
        assertTrue(state.selectedProviders.any { it.id == "1" })
        assertTrue(state.selectedProviders.any { it.id == "2" })
    }

    @Test
    fun `runComparison does nothing when query is blank`() = runTest {
        val providers = listOf(
            createProvider(id = "1", name = "P1", apiKey = "k1")
        )
        providersFlow.value = providers
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setQuery("  ")
        viewModel.runComparison()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRunning)
        assertTrue(viewModel.uiState.value.results.isEmpty())
    }

    @Test
    fun `runComparison does nothing when no providers selected`() = runTest {
        viewModel.setQuery("Hello")
        viewModel.runComparison()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRunning)
        assertTrue(viewModel.uiState.value.results.isEmpty())
    }

    @Test
    fun `runComparison starts comparison with multiple providers`() = runTest {
        val provider1 = createProvider(id = "1", name = "OpenAI", apiKey = "k1")
        val provider2 = createProvider(id = "2", name = "Anthropic", apiKey = "k2")
        providersFlow.value = listOf(provider1, provider2)
        testDispatcher.scheduler.advanceUntilIdle()

        val aiProvider = mockk<AIProvider>(relaxed = true)
        coEvery { aiProviderManager.getProvider(any()) } returns aiProvider
        coEvery { aiProvider.sendMessage(any(), any(), any(), any()) } returns AIResponse(
            content = "Response from AI",
            model = "gpt-4",
            usage = null
        )

        viewModel.setQuery("What is AI?")
        viewModel.runComparison()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRunning)
        assertEquals(2, state.results.size)
        assertNotNull(state.sessions)
        assertTrue(state.sessions.isNotEmpty())
        assertNotNull(state.selectedSession)
    }

    @Test
    fun `runComparison handles provider error`() = runTest {
        val provider = createProvider(id = "1", name = "FailingProvider", apiKey = "k1")
        providersFlow.value = listOf(provider)
        testDispatcher.scheduler.advanceUntilIdle()

        val aiProvider = mockk<AIProvider>(relaxed = true)
        coEvery { aiProviderManager.getProvider(any()) } returns aiProvider
        coEvery { aiProvider.sendMessage(any(), any(), any(), any()) } throws RuntimeException("API error")

        viewModel.setQuery("Test query")
        viewModel.runComparison()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRunning)
        assertEquals(1, state.results.size)
        assertNotNull(state.results[0].error)
        assertTrue(state.results[0].error!!.contains("API error"))
    }

    @Test
    fun `selectWinner with selected session updates session`() = runTest {
        val provider = createProvider(id = "1", name = "P1", apiKey = "k1")
        providersFlow.value = listOf(provider)
        testDispatcher.scheduler.advanceUntilIdle()

        val aiProvider = mockk<AIProvider>(relaxed = true)
        coEvery { aiProviderManager.getProvider(any()) } returns aiProvider
        coEvery { aiProvider.sendMessage(any(), any(), any(), any()) } returns AIResponse(
            content = "Response", model = "gpt-4"
        )

        viewModel.setQuery("Test")
        viewModel.runComparison()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectWinner("1")
        val state = viewModel.uiState.value

        assertNotNull(state.selectedSession)
        assertEquals("1", state.selectedSession!!.selectedWinner)
    }

    @Test
    fun `selectWinner without session creates new session`() = runTest {
        val providers = listOf(
            createProvider(id = "1", name = "P1", apiKey = "k1")
        )
        providersFlow.value = providers
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setQuery("Test query")
        viewModel.selectWinner("1")
        val state = viewModel.uiState.value

        assertEquals(1, state.sessions.size)
        assertEquals("1", state.sessions[0].selectedWinner)
        assertEquals("Test query", state.sessions[0].query)
    }

    @Test
    fun `rateResult updates result rating`() = runTest {
        val provider = createProvider(id = "1", name = "P1", apiKey = "k1")
        providersFlow.value = listOf(provider)
        testDispatcher.scheduler.advanceUntilIdle()

        val aiProvider = mockk<AIProvider>(relaxed = true)
        coEvery { aiProviderManager.getProvider(any()) } returns aiProvider
        coEvery { aiProvider.sendMessage(any(), any(), any(), any()) } returns AIResponse(
            content = "Response", model = "gpt-4"
        )

        viewModel.setQuery("Test")
        viewModel.runComparison()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.rateResult("1", 5)
        assertEquals(5, viewModel.uiState.value.results[0].rating)
    }

    @Test
    fun `rateResult does nothing for nonexistent provider`() = runTest {
        viewModel.rateResult("nonexistent", 5)
        assertTrue(viewModel.uiState.value.results.isEmpty())
    }

    @Test
    fun `ComparisonMode count is correct`() {
        assertEquals(2, ComparisonMode.TWO.count)
        assertEquals(3, ComparisonMode.THREE.count)
        assertEquals(4, ComparisonMode.FOUR.count)
    }

    @Test
    fun `ComparisonMode displayName is correct`() {
        assertEquals("2 AI", ComparisonMode.TWO.displayName)
        assertEquals("3 AI", ComparisonMode.THREE.displayName)
        assertEquals("4 AI", ComparisonMode.FOUR.displayName)
    }
}
