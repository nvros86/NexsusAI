package com.nexusai.app.ui

import android.app.Application
import com.nexusai.domain.model.MarketplaceCategory
import com.nexusai.domain.model.MarketplaceProvider
import com.nexusai.domain.model.ProviderCapability
import com.nexusai.domain.model.ProviderType
import com.nexusai.domain.repository.AIProviderRepository
import com.nexusai.domain.repository.MarketplaceRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MarketplaceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var marketplaceRepository: MarketplaceRepository
    private lateinit var aiProviderRepository: AIProviderRepository
    private lateinit var viewModel: MarketplaceViewModel
    private val mockApplication = mockk<Application>(relaxed = true)

    private val presetsFlow = MutableStateFlow<List<MarketplaceProvider>>(emptyList())

    private fun createPreset(
        id: String = "preset-1",
        name: String = "Test Provider",
        type: ProviderType = ProviderType.OPENAI,
        category: MarketplaceCategory = MarketplaceCategory.TEXT,
        models: List<String> = listOf("gpt-4"),
        defaultModel: String = "gpt-4",
        capabilities: List<ProviderCapability> = listOf(ProviderCapability.TEXT_GENERATION)
    ) = MarketplaceProvider(
        id = id,
        name = name,
        description = "A test provider",
        type = type,
        baseUrl = "https://api.test.com",
        models = models,
        defaultModel = defaultModel,
        category = category,
        capabilities = capabilities,
        logoEmoji = "🤖"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        marketplaceRepository = mockk(relaxed = true)
        aiProviderRepository = mockk(relaxed = true)

        every { marketplaceRepository.getAllPresets() } returns presetsFlow
        every { marketplaceRepository.searchPresets(any()) } returns flowOf(emptyList())
        every { marketplaceRepository.getPresetsByCategory(any()) } returns flowOf(emptyList())
        every { mockApplication.getString(any<Int>()) } answers { "Test string" }
        every { mockApplication.getString(any<Int>(), any()) } answers { "Test string" }

        viewModel = MarketplaceViewModel(mockApplication, marketplaceRepository, aiProviderRepository)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        val state = viewModel.uiState.value
        assertTrue(state.providers.isEmpty())
        assertEquals("", state.searchQuery)
        assertNull(state.selectedCategory)
        assertFalse(state.isSearching)
        assertNull(state.addedProviderName)
        assertNull(state.error)
    }

    @Test
    fun `init loads providers from repository`() = runTest {
        val presets = listOf(createPreset(id = "1", name = "Alpha"), createPreset(id = "2", name = "Beta"))
        presetsFlow.value = presets
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.providers.size)
        assertEquals("Alpha", state.providers[0].name)
        assertEquals("Beta", state.providers[1].name)
    }

    @Test
    fun `search updates searchQuery and isSearching`() = runTest {
        val filtered = listOf(createPreset(id = "f1", name = "Filtered"))
        every { marketplaceRepository.searchPresets("test") } returns flowOf(filtered)

        viewModel.search("test")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("test", state.searchQuery)
        assertTrue(state.isSearching)
        assertEquals(1, state.providers.size)
        assertEquals("Filtered", state.providers[0].name)
    }

    @Test
    fun `search with empty query clears isSearching`() = runTest {
        every { marketplaceRepository.getAllPresets() } returns flowOf(listOf(createPreset()))

        viewModel.search("test")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSearching)

        viewModel.search("")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSearching)
        assertEquals("", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `search calls searchPresets on repository`() = runTest {
        every { marketplaceRepository.searchPresets("openai") } returns flowOf(
            listOf(createPreset(id = "s1", name = "OpenAI"))
        )

        viewModel.search("openai")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { marketplaceRepository.searchPresets("openai") }
    }

    @Test
    fun `selectCategory updates selectedCategory`() = runTest {
        val imageProviders = listOf(createPreset(id = "i1", name = "Image AI", category = MarketplaceCategory.IMAGE))
        every { marketplaceRepository.getPresetsByCategory("IMAGE") } returns flowOf(imageProviders)

        viewModel.selectCategory("IMAGE")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("IMAGE", viewModel.uiState.value.selectedCategory)
        assertEquals(1, viewModel.uiState.value.providers.size)
        assertEquals("Image AI", viewModel.uiState.value.providers[0].name)
    }

    @Test
    fun `selectCategory with null loads all presets`() = runTest {
        val all = listOf(createPreset(id = "a1", name = "All"))
        every { marketplaceRepository.getAllPresets() } returns flowOf(all)

        viewModel.selectCategory(null)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedCategory)
        coVerify { marketplaceRepository.getAllPresets() }
    }

    @Test
    fun `selectCategory calls getPresetsByCategory on repository`() = runTest {
        every { marketplaceRepository.getPresetsByCategory("CODE") } returns flowOf(
            listOf(createPreset(id = "c1", name = "Code AI", category = MarketplaceCategory.CODE))
        )

        viewModel.selectCategory("CODE")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { marketplaceRepository.getPresetsByCategory("CODE") }
    }

    @Test
    fun `addProvider adds provider to favorites and marks as added`() = runTest {
        val preset = createPreset(id = "preset-add", name = "New Provider")

        viewModel.addProvider(preset, apiKey = "my-api-key")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            aiProviderRepository.addProvider(match {
                it.name == "New Provider" && it.apiKey == "my-api-key"
            })
        }
        coVerify { marketplaceRepository.markAsAdded("preset-add") }
        assertEquals("New Provider", viewModel.uiState.value.addedProviderName)
    }

    @Test
    fun `addProvider with empty apiKey succeeds`() = runTest {
        val preset = createPreset(id = "p-empty", name = "Empty Key Provider")

        viewModel.addProvider(preset, apiKey = "")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            aiProviderRepository.addProvider(match {
                it.name == "Empty Key Provider" && it.apiKey == ""
            })
        }
        assertEquals("Empty Key Provider", viewModel.uiState.value.addedProviderName)
    }

    @Test
    fun `addProvider sets error on failure`() = runTest {
        val preset = createPreset(id = "p-fail", name = "Fail Provider")
        coEvery { aiProviderRepository.addProvider(any()) } throws RuntimeException("DB error")

        viewModel.addProvider(preset)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Ошибка добавления провайдера"))
        assertNull(state.addedProviderName)
    }

    @Test
    fun `addProvider maps capabilities correctly`() = runTest {
        val preset = createPreset(
            id = "p-caps",
            name = "Capable Provider",
            capabilities = listOf(ProviderCapability.VISION, ProviderCapability.STREAMING, ProviderCapability.CODE_GENERATION)
        )

        viewModel.addProvider(preset)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            aiProviderRepository.addProvider(match {
                it.supportsImages && it.supportsStreaming
            })
        }
    }

    @Test
    fun `clearError resets error to null`() = runTest {
        coEvery { aiProviderRepository.addProvider(any()) } throws RuntimeException("error")
        val preset = createPreset()
        viewModel.addProvider(preset)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `dismissAddedMessage resets addedProviderName`() = runTest {
        val preset = createPreset(name = "Provider To Dismiss")
        viewModel.addProvider(preset)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Provider To Dismiss", viewModel.uiState.value.addedProviderName)

        viewModel.dismissAddedMessage()
        assertNull(viewModel.uiState.value.addedProviderName)
    }

    @Test
    fun `presets flow updates are reflected in state`() = runTest {
        assertTrue(viewModel.uiState.value.providers.isEmpty())

        presetsFlow.value = listOf(createPreset(id = "1", name = "First"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.providers.size)

        presetsFlow.value = listOf(
            createPreset(id = "1", name = "First"),
            createPreset(id = "2", name = "Second")
        )
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.providers.size)
    }

    @Test
    fun `search then selectCategory switches filter`() = runTest {
        every { marketplaceRepository.searchPresets("gpt") } returns flowOf(
            listOf(createPreset(id = "s1", name = "GPT Provider"))
        )
        every { marketplaceRepository.getPresetsByCategory("CODE") } returns flowOf(
            listOf(createPreset(id = "c1", name = "Code Provider", category = MarketplaceCategory.CODE))
        )

        viewModel.search("gpt")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("GPT Provider", viewModel.uiState.value.providers[0].name)

        viewModel.selectCategory("CODE")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Code Provider", viewModel.uiState.value.providers[0].name)
        assertEquals("CODE", viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `addProvider with different categories works`() = runTest {
        val imagePreset = createPreset(
            id = "img-1",
            name = "DALL-E",
            category = MarketplaceCategory.IMAGE,
            capabilities = listOf(ProviderCapability.IMAGE_GENERATION)
        )

        viewModel.addProvider(imagePreset, apiKey = "dalle-key")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            aiProviderRepository.addProvider(match {
                it.name == "DALL-E" && it.type == ProviderType.STABILITY
            })
        }
    }
}
