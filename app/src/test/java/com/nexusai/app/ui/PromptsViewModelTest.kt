package com.nexusai.app.ui

import com.nexusai.domain.model.Prompt
import com.nexusai.domain.model.PromptCategory
import com.nexusai.domain.repository.PromptRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
class PromptsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var promptRepository: PromptRepository
    private lateinit var viewModel: PromptsViewModel

    private val promptsFlow = MutableStateFlow<List<Prompt>>(emptyList())

    private fun createPrompt(
        id: String = "prompt-1",
        title: String = "Test Prompt",
        description: String = "A test prompt",
        content: String = "Write something",
        category: PromptCategory = PromptCategory.WRITING,
        tags: List<String> = emptyList(),
        usageCount: Int = 0,
        isFavorite: Boolean = false
    ) = Prompt(
        id = id,
        title = title,
        description = description,
        content = content,
        category = category,
        tags = tags,
        usageCount = usageCount,
        isFavorite = isFavorite
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        promptRepository = mockk(relaxed = true)
        every { promptRepository.getAllPrompts() } returns promptsFlow
        every { promptRepository.getPromptsByCategory(any()) } returns flowOf(emptyList())
        every { promptRepository.searchPrompts(any()) } returns flowOf(emptyList())
        every { promptRepository.getFavoritePrompts() } returns flowOf(emptyList())
        viewModel = PromptsViewModel(promptRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        val state = viewModel.uiState.value
        assertTrue(state.prompts.isEmpty())
        assertEquals("", state.searchQuery)
        assertNull(state.selectedCategory)
        assertFalse(state.showFavoritesOnly)
        assertNull(state.copiedPromptId)
    }

    @Test
    fun `init loads prompts from repository`() = runTest {
        val prompts = listOf(
            createPrompt(id = "1", title = "Prompt A"),
            createPrompt(id = "2", title = "Prompt B")
        )
        promptsFlow.value = prompts
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.prompts.size)
        assertEquals("Prompt A", state.prompts[0].title)
        assertEquals("Prompt B", state.prompts[1].title)
    }

    @Test
    fun `search updates searchQuery in state`() {
        viewModel.search("kotlin")
        assertEquals("kotlin", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `search with query calls searchPrompts`() = runTest {
        val results = listOf(createPrompt(id = "1", title = "Kotlin"))
        every { promptRepository.searchPrompts("kotlin") } returns flowOf(results)

        viewModel.search("kotlin")
        testDispatcher.scheduler.advanceUntilIdle()

        verify { promptRepository.searchPrompts("kotlin") }
        assertEquals(1, viewModel.uiState.value.prompts.size)
    }

    @Test
    fun `search with empty query and no favorites loads all prompts`() = runTest {
        val allPrompts = listOf(createPrompt(id = "1"), createPrompt(id = "2"))
        every { promptRepository.getAllPrompts() } returns flowOf(allPrompts)

        viewModel.search("")
        testDispatcher.scheduler.advanceUntilIdle()

        verify { promptRepository.getAllPrompts() }
        assertEquals(2, viewModel.uiState.value.prompts.size)
    }

    @Test
    fun `search with empty query and favorites only loads favorites`() = runTest {
        viewModel.toggleFavoritesOnly()
        testDispatcher.scheduler.advanceUntilIdle()

        val favPrompts = listOf(createPrompt(id = "fav-1", isFavorite = true))
        every { promptRepository.getFavoritePrompts() } returns flowOf(favPrompts)

        viewModel.search("")
        testDispatcher.scheduler.advanceUntilIdle()

        verify { promptRepository.getFavoritePrompts() }
    }

    @Test
    fun `selectCategory updates selectedCategory and clears showFavoritesOnly`() = runTest {
        viewModel.toggleFavoritesOnly()
        assertTrue(viewModel.uiState.value.showFavoritesOnly)

        viewModel.selectCategory("coding")
        assertEquals("coding", viewModel.uiState.value.selectedCategory)
        assertFalse(viewModel.uiState.value.showFavoritesOnly)
    }

    @Test
    fun `selectCategory null loads all prompts`() = runTest {
        viewModel.selectCategory(null)
        testDispatcher.scheduler.advanceUntilIdle()

        verify { promptRepository.getAllPrompts() }
    }

    @Test
    fun `selectCategory non-null calls getPromptsByCategory`() = runTest {
        viewModel.selectCategory("marketing")
        testDispatcher.scheduler.advanceUntilIdle()

        verify { promptRepository.getPromptsByCategory("marketing") }
    }

    @Test
    fun `toggleFavoritesOnly toggles showFavoritesOnly`() = runTest {
        assertFalse(viewModel.uiState.value.showFavoritesOnly)

        viewModel.toggleFavoritesOnly()
        assertTrue(viewModel.uiState.value.showFavoritesOnly)

        viewModel.toggleFavoritesOnly()
        assertFalse(viewModel.uiState.value.showFavoritesOnly)
    }

    @Test
    fun `toggleFavoritesOnly clears selectedCategory`() = runTest {
        viewModel.selectCategory("coding")
        assertNotNull(viewModel.uiState.value.selectedCategory)

        viewModel.toggleFavoritesOnly()
        assertNull(viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `toggleFavorite calls repository`() = runTest {
        viewModel.toggleFavorite("prompt-1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { promptRepository.toggleFavorite("prompt-1") }
    }

    @Test
    fun `copyPrompt calls incrementUsage and sets copiedPromptId`() = runTest {
        viewModel.copyPrompt("prompt-1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { promptRepository.incrementUsage("prompt-1") }
        assertEquals("prompt-1", viewModel.uiState.value.copiedPromptId)
    }

    @Test
    fun `dismissCopied clears copiedPromptId`() = runTest {
        viewModel.copyPrompt("prompt-1")
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.copiedPromptId)

        viewModel.dismissCopied()
        assertNull(viewModel.uiState.value.copiedPromptId)
    }

    @Test
    fun `prompts flow updates are reflected in state`() = runTest {
        assertTrue(viewModel.uiState.value.prompts.isEmpty())

        promptsFlow.value = listOf(createPrompt(id = "1", title = "First"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.prompts.size)

        promptsFlow.value = listOf(
            createPrompt(id = "1", title = "First"),
            createPrompt(id = "2", title = "Second")
        )
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.prompts.size)
    }
}
