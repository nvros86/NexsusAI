package com.nexusai.app.ui

import com.nexusai.data.ai.AIRouter
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.AIRoutingResult
import com.nexusai.domain.model.ProviderType
import com.nexusai.domain.model.RoutingStrategy
import com.nexusai.domain.repository.AIProviderRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
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
class AIRouterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var aiRouter: AIRouter
    private lateinit var providerRepository: AIProviderRepository
    private lateinit var viewModel: AIRouterViewModel

    private val providersFlow = MutableStateFlow<List<AIProviderConfig>>(emptyList())

    private fun createProvider(
        id: String = "provider-1",
        name: String = "OpenAI",
        type: ProviderType = ProviderType.OPENAI,
        apiKey: String = "sk-test"
    ) = AIProviderConfig(
        id = id,
        name = name,
        type = type,
        baseUrl = "https://api.openai.com",
        apiKey = apiKey
    )

    private fun createRoutingResult(
        provider: AIProviderConfig = createProvider(),
        strategy: RoutingStrategy = RoutingStrategy.BALANCED
    ) = AIRoutingResult(
        selectedProvider = provider,
        selectedModel = "gpt-4o",
        strategy = strategy,
        score = 0.85f,
        failoverChain = emptyList(),
        reason = "Best balanced option"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        aiRouter = mockk(relaxed = true)
        providerRepository = mockk(relaxed = true)
        every { providerRepository.getAllProviders() } returns providersFlow
        viewModel = AIRouterViewModel(aiRouter, providerRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        val state = viewModel.uiState.value
        assertTrue(state.providers.isEmpty())
        assertEquals(RoutingStrategy.BALANCED, state.strategy)
        assertNull(state.routingResult)
        assertFalse(state.isRouting)
        assertEquals("", state.testMessage)
        assertNull(state.lastError)
    }

    @Test
    fun `init loads providers from repository`() = runTest {
        val providers = listOf(
            createProvider(id = "1", name = "OpenAI"),
            createProvider(id = "2", name = "Anthropic", type = ProviderType.ANTHROPIC)
        )
        providersFlow.value = providers
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.providers.size)
        assertEquals("OpenAI", state.providers[0].name)
        assertEquals("Anthropic", state.providers[1].name)
    }

    @Test
    fun `setStrategy updates strategy`() = runTest {
        coEvery { providerRepository.getAllProviders() } returns flowOf(emptyList())
        coEvery { aiRouter.route(any(), any()) } returns null

        viewModel.setStrategy(RoutingStrategy.BEST_QUALITY)
        assertEquals(RoutingStrategy.BEST_QUALITY, viewModel.uiState.value.strategy)
    }

    @Test
    fun `setStrategy triggers runRouting`() = runTest {
        coEvery { providerRepository.getAllProviders() } returns flowOf(emptyList())
        coEvery { aiRouter.route(any(), any()) } returns null

        viewModel.setStrategy(RoutingStrategy.CHEAPEST)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRouting)
    }

    @Test
    fun `runRouting success sets routingResult`() = runTest {
        val providers = listOf(createProvider())
        providersFlow.value = providers
        val result = createRoutingResult()
        coEvery { aiRouter.route(any(), any()) } returns result

        viewModel.runRouting()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRouting)
        assertNotNull(state.routingResult)
        assertEquals("gpt-4o", state.routingResult?.selectedModel)
    }

    @Test
    fun `runRouting failure sets lastError`() = runTest {
        coEvery { providerRepository.getAllProviders() } returns flowOf(listOf(createProvider()))
        coEvery { aiRouter.route(any(), any()) } throws RuntimeException("Router failed")

        viewModel.runRouting()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRouting)
        assertNotNull(state.lastError)
        assertEquals("Router failed", state.lastError)
    }

    @Test
    fun `runRouting completes and resets isRouting`() = runTest {
        coEvery { providerRepository.getAllProviders() } returns flowOf(listOf(createProvider()))
        coEvery { aiRouter.route(any(), any()) } returns createRoutingResult()

        viewModel.runRouting()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRouting)
        assertNotNull(viewModel.uiState.value.routingResult)
    }

    @Test
    fun `runRouting clears lastError before routing`() = runTest {
        coEvery { providerRepository.getAllProviders() } returns flowOf(listOf(createProvider()))
        coEvery { aiRouter.route(any(), any()) } throws RuntimeException("error")
        viewModel.runRouting()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.lastError)

        coEvery { aiRouter.route(any(), any()) } returns createRoutingResult()
        viewModel.runRouting()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.lastError)
    }

    @Test
    fun `setTestMessage updates testMessage`() {
        viewModel.setTestMessage("Hello, AI!")
        assertEquals("Hello, AI!", viewModel.uiState.value.testMessage)
    }

    @Test
    fun `setTestMessage can be called multiple times`() {
        viewModel.setTestMessage("First")
        viewModel.setTestMessage("Second")
        assertEquals("Second", viewModel.uiState.value.testMessage)
    }

    @Test
    fun `testRoute calls runRouting`() = runTest {
        coEvery { providerRepository.getAllProviders() } returns flowOf(listOf(createProvider()))
        coEvery { aiRouter.route(any(), any()) } returns createRoutingResult()

        viewModel.testRoute()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.routingResult)
    }
}
