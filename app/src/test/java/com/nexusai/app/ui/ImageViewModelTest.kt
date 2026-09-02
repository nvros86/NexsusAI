package com.nexusai.app.ui

import com.nexusai.data.ai.AIProviderManager
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.repository.AIProviderRepository
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
class ImageViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var providerRepository: AIProviderRepository
    private lateinit var aiProviderManager: AIProviderManager
    private lateinit var viewModel: ImageViewModel

    private val providersFlow = MutableStateFlow<List<AIProviderConfig>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        providerRepository = mockk(relaxed = true)
        aiProviderManager = mockk(relaxed = true)
        every { providerRepository.getAllProviders() } returns providersFlow
        viewModel = ImageViewModel(providerRepository, aiProviderManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        val state = viewModel.uiState.value
        assertEquals("", state.prompt)
        assertFalse(state.isGenerating)
        assertTrue(state.images.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `setPrompt updates prompt`() {
        viewModel.setPrompt("A cat in space")
        assertEquals("A cat in space", viewModel.uiState.value.prompt)
    }

    @Test
    fun `setPrompt can be called multiple times`() {
        viewModel.setPrompt("First prompt")
        viewModel.setPrompt("Second prompt")
        assertEquals("Second prompt", viewModel.uiState.value.prompt)
    }

    @Test
    fun `generate does nothing when prompt is blank`() = runTest {
        viewModel.setPrompt("  ")
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isGenerating)
        assertTrue(state.images.isEmpty())
    }

    @Test
    fun `generate does nothing when prompt is empty`() = runTest {
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isGenerating)
        assertTrue(state.images.isEmpty())
    }

    @Test
    fun `generate sets generating state`() = runTest {
        viewModel.setPrompt("Generate an image")
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isGenerating)
        assertTrue(state.images.isNotEmpty())
        assertEquals("", state.prompt)
    }

    @Test
    fun `generate clears prompt after adding image`() = runTest {
        viewModel.setPrompt("A dog")
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.prompt)
    }

    @Test
    fun `generate adds image with correct prompt`() = runTest {
        viewModel.setPrompt("A beautiful sunset")
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        val images = viewModel.uiState.value.images
        assertEquals(1, images.size)
        assertEquals("A beautiful sunset", images[0].prompt)
        assertNotNull(images[0].id)
        assertFalse(images[0].isFavorite)
        assertNull(images[0].url)
    }

    @Test
    fun `generate with multiple prompts adds multiple images`() = runTest {
        viewModel.setPrompt("First image")
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setPrompt("Second image")
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.images.size)
        assertEquals("First image", viewModel.uiState.value.images[0].prompt)
        assertEquals("Second image", viewModel.uiState.value.images[1].prompt)
    }

    @Test
    fun `toggleFavorite toggles favorite status`() = runTest {
        viewModel.setPrompt("Test image")
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        val imageId = viewModel.uiState.value.images[0].id
        assertFalse(viewModel.uiState.value.images[0].isFavorite)

        viewModel.toggleFavorite(imageId)
        assertTrue(viewModel.uiState.value.images[0].isFavorite)

        viewModel.toggleFavorite(imageId)
        assertFalse(viewModel.uiState.value.images[0].isFavorite)
    }

    @Test
    fun `toggleFavorite does nothing for nonexistent id`() = runTest {
        viewModel.setPrompt("Test image")
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        val originalFavorite = viewModel.uiState.value.images[0].isFavorite
        viewModel.toggleFavorite("nonexistent-id")
        assertEquals(originalFavorite, viewModel.uiState.value.images[0].isFavorite)
    }

    @Test
    fun `deleteImage removes image from list`() = runTest {
        viewModel.setPrompt("Image 1")
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setPrompt("Image 2")
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.images.size)

        val idToDelete = viewModel.uiState.value.images[0].id
        viewModel.deleteImage(idToDelete)

        assertEquals(1, viewModel.uiState.value.images.size)
        assertFalse(viewModel.uiState.value.images.any { it.id == idToDelete })
    }

    @Test
    fun `deleteImage does nothing for nonexistent id`() = runTest {
        viewModel.setPrompt("Test")
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        val sizeBefore = viewModel.uiState.value.images.size
        viewModel.deleteImage("nonexistent-id")
        assertEquals(sizeBefore, viewModel.uiState.value.images.size)
    }

    @Test
    fun `clearImages resets images list`() = runTest {
        viewModel.setPrompt("Image 1")
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setPrompt("Image 2")
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.images.size)

        viewModel.clearImages()
        assertTrue(viewModel.uiState.value.images.isEmpty())
    }

    @Test
    fun `clearImages on empty list does nothing`() {
        assertTrue(viewModel.uiState.value.images.isEmpty())
        viewModel.clearImages()
        assertTrue(viewModel.uiState.value.images.isEmpty())
    }

    @Test
    fun `dismissError clears error`() = runTest {
        viewModel.setPrompt("  ")
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.dismissError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `dismissError on null error does nothing`() {
        assertNull(viewModel.uiState.value.error)
        viewModel.dismissError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `GeneratedImage has correct defaults`() {
        val image = GeneratedImage(id = "1", prompt = "test")
        assertNull(image.url)
        assertFalse(image.isFavorite)
        assertNotNull(image.createdAt)
        assertTrue(image.createdAt > 0)
    }

    @Test
    fun `GeneratedImage copy preserves fields`() {
        val original = GeneratedImage(id = "1", prompt = "test", url = "http://example.com/img.png")
        val copied = original.copy(isFavorite = true)

        assertEquals(original.id, copied.id)
        assertEquals(original.prompt, copied.prompt)
        assertEquals(original.url, copied.url)
        assertTrue(copied.isFavorite)
    }
}
