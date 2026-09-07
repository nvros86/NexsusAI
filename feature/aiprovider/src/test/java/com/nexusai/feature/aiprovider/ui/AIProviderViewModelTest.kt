package com.nexusai.feature.aiprovider.ui

import androidx.lifecycle.SavedStateHandle
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.ProviderType
import com.nexusai.domain.repository.AIProviderRepository
import io.mockk.coEvery
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
class AIProviderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var providerRepository: AIProviderRepository

    private fun createProvider(
        id: String = "provider-1",
        name: String = "OpenAI",
        type: ProviderType = ProviderType.OPENAI
    ) = AIProviderConfig(
        id = id,
        name = name,
        type = type,
        baseUrl = "https://api.openai.com",
        apiKey = "sk-test"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        providerRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(providerId: String = "provider-1"): AIProviderViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("providerId" to providerId))
        return AIProviderViewModel(savedStateHandle, providerRepository)
    }

    @Test
    fun `initial state has default values`() = runTest {
        val viewModel = createViewModel()
        val state = viewModel.state.value

        assertNull(state.provider)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `init with empty providerId does not load`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("providerId" to ""))
        val viewModel = AIProviderViewModel(savedStateHandle, providerRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.state.value.provider)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `init loads provider by id`() = runTest {
        val provider = createProvider(id = "provider-1", name = "OpenAI")
        coEvery { providerRepository.getProviderById("provider-1") } returns provider

        val viewModel = createViewModel("provider-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.provider)
        assertEquals("OpenAI", state.provider?.name)
        assertFalse(state.isLoading)
    }

    @Test
    fun `init completes loading with provider`() = runTest {
        val provider = createProvider()
        coEvery { providerRepository.getProviderById(any()) } returns provider

        val viewModel = createViewModel("provider-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.provider)
        assertFalse(state.isLoading)
    }

    @Test
    fun `init sets error on failure`() = runTest {
        coEvery { providerRepository.getProviderById("provider-1") } throws RuntimeException("Provider not found")

        val viewModel = createViewModel("provider-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.error)
        assertEquals("Provider not found", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `init with different provider id loads correct provider`() = runTest {
        val provider = createProvider(id = "anthropic-1", name = "Anthropic", type = ProviderType.ANTHROPIC)
        coEvery { providerRepository.getProviderById("anthropic-1") } returns provider

        val viewModel = createViewModel("anthropic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Anthropic", viewModel.state.value.provider?.name)
        assertEquals("anthropic-1", viewModel.state.value.provider?.id)
    }

    @Test
    fun `provider is null when repository returns null`() = runTest {
        coEvery { providerRepository.getProviderById("unknown") } returns null

        val viewModel = createViewModel("unknown")
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.state.value.provider)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `error message is preserved after loading`() = runTest {
        coEvery { providerRepository.getProviderById("bad") } throws RuntimeException("Connection timeout")

        val viewModel = createViewModel("bad")
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.error!!.contains("Connection timeout"))
    }
}
