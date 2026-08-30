package com.nexusai.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class PromptRepositoryImplTest {

    private lateinit var repository: PromptRepositoryImpl
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
        repository = PromptRepositoryImpl()
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `getAllPrompts returns all 16 prompts`() = runTest {
        val prompts = repository.getAllPrompts().first()
        assertEquals(16, prompts.size)
    }

    @Test
    fun `getAllPrompts returns prompts with default values`() = runTest {
        val prompts = repository.getAllPrompts().first()
        val first = prompts.first { it.id == "write_email" }
        assertEquals("Профессиональное письмо", first.title)
        assertFalse(first.isFavorite)
        assertEquals(0, first.usageCount)
    }

    @Test
    fun `getPromptsByCategory WRITING returns 2 prompts`() = runTest {
        val prompts = repository.getPromptsByCategory("WRITING").first()
        assertEquals(2, prompts.size)
        assertTrue(prompts.all { it.category.name == "WRITING" })
    }

    @Test
    fun `getPromptsByCategory CODING returns 3 prompts`() = runTest {
        val prompts = repository.getPromptsByCategory("CODING").first()
        assertEquals(3, prompts.size)
        assertTrue(prompts.all { it.category.name == "CODING" })
    }

    @Test
    fun `getPromptsByCategory MARKETING returns 2 prompts`() = runTest {
        val prompts = repository.getPromptsByCategory("MARKETING").first()
        assertEquals(2, prompts.size)
    }

    @Test
    fun `getPromptsByCategory ANALYSIS returns 2 prompts`() = runTest {
        val prompts = repository.getPromptsByCategory("ANALYSIS").first()
        assertEquals(2, prompts.size)
    }

    @Test
    fun `getPromptsByCategory EDUCATION returns 2 prompts`() = runTest {
        val prompts = repository.getPromptsByCategory("EDUCATION").first()
        assertEquals(2, prompts.size)
    }

    @Test
    fun `getPromptsByCategory nonexistent returns empty`() = runTest {
        val prompts = repository.getPromptsByCategory("NONEXISTENT").first()
        assertTrue(prompts.isEmpty())
    }

    @Test
    fun `searchPrompts by title matches`() = runTest {
        val prompts = repository.searchPrompts("письмо").first()
        assertTrue(prompts.isNotEmpty())
        assertTrue(prompts.any { it.id == "write_email" })
    }

    @Test
    fun `searchPrompts by description matches`() = runTest {
        val prompts = repository.searchPrompts("резюме").first()
        assertTrue(prompts.isNotEmpty())
    }

    @Test
    fun `searchPrompts by tag matches`() = runTest {
        val prompts = repository.searchPrompts("дебаг").first()
        assertTrue(prompts.isNotEmpty())
        assertTrue(prompts.any { it.id == "debug_code" })
    }

    @Test
    fun `searchPrompts case insensitive`() = runTest {
        val lower = repository.searchPrompts("код").first()
        val upper = repository.searchPrompts("КОД").first()
        assertEquals(lower.size, upper.size)
    }

    @Test
    fun `searchPrompts no match returns empty`() = runTest {
        val prompts = repository.searchPrompts("несуществующий_запрос_xyz").first()
        assertTrue(prompts.isEmpty())
    }

    @Test
    fun `getFavoritePrompts returns empty when no favorites`() = runTest {
        val favorites = repository.getFavoritePrompts().first()
        assertTrue(favorites.isEmpty())
    }

    @Test
    fun `toggleFavorite adds favorite`() = runTest {
        repository.toggleFavorite("write_email")
        val favorites = repository.getFavoritePrompts().first()
        assertEquals(1, favorites.size)
        assertEquals("write_email", favorites.first().id)
        assertTrue(favorites.first().isFavorite)
    }

    @Test
    fun `toggleFavorite removes favorite`() = runTest {
        repository.toggleFavorite("write_email")
        repository.toggleFavorite("write_email")
        val favorites = repository.getFavoritePrompts().first()
        assertTrue(favorites.isEmpty())
    }

    @Test
    fun `incrementUsage increments count`() = runTest {
        repository.incrementUsage("write_email")
        repository.incrementUsage("write_email")
        repository.incrementUsage("write_email")
        val prompts = repository.getAllPrompts().first()
        val prompt = prompts.first { it.id == "write_email" }
        assertEquals(3, prompt.usageCount)
    }

    @Test
    fun `incrementUsage does not affect other prompts`() = runTest {
        repository.incrementUsage("write_email")
        val prompts = repository.getAllPrompts().first()
        val otherPrompt = prompts.first { it.id == "write_blog" }
        assertEquals(0, otherPrompt.usageCount)
    }

    @Test
    fun `getAllPrompts reflects favorite state`() = runTest {
        repository.toggleFavorite("code_review")
        val prompts = repository.getAllPrompts().first()
        val review = prompts.first { it.id == "code_review" }
        assertTrue(review.isFavorite)
        val other = prompts.first { it.id == "write_email" }
        assertFalse(other.isFavorite)
    }

    @Test
    fun `getAllPrompts reflects usage count`() = runTest {
        repository.incrementUsage("brainstorm")
        val prompts = repository.getAllPrompts().first()
        val brainstorm = prompts.first { it.id == "brainstorm" }
        assertEquals(1, brainstorm.usageCount)
    }

    @Test
    fun `searchPrompts with empty query returns all`() = runTest {
        val prompts = repository.searchPrompts("").first()
        assertEquals(16, prompts.size)
    }

    @Test
    fun `all categories are covered`() = runTest {
        val allPrompts = repository.getAllPrompts().first()
        val categories = allPrompts.map { it.category.name }.toSet()
        assertEquals(9, categories.size)
        assertTrue(categories.containsAll(setOf(
            "WRITING", "BUSINESS", "CODING", "MARKETING",
            "TRANSLATION", "CREATIVE", "ANALYSIS", "EDUCATION", "DAILY"
        )))
    }
}
