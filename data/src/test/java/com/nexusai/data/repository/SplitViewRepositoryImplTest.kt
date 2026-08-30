package com.nexusai.data.repository

import android.content.Context
import com.nexusai.domain.model.ComparisonMode
import com.nexusai.domain.model.SplitResult
import com.nexusai.domain.model.SplitSession
import io.mockk.every
import io.mockk.mockk
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
class SplitViewRepositoryImplTest {

    private lateinit var repository: SplitViewRepositoryImpl
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private val mockContext = mockk<Context>()

    @Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
        every { mockContext.applicationContext } returns mockContext
        repository = SplitViewRepositoryImpl(mockContext)
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `getAllSessions returns empty initially`() = runTest {
        val sessions = repository.getAllSessions().first()
        assertTrue(sessions.isEmpty())
    }

    @Test
    fun `saveSession adds session`() = runTest {
        val session = SplitSession(
            id = "test-1",
            query = "Hello AI",
            results = emptyList(),
            timestamp = System.currentTimeMillis()
        )
        repository.saveSession(session)
        val sessions = repository.getAllSessions().first()
        assertEquals(1, sessions.size)
        assertEquals("test-1", sessions.first().id)
    }

    @Test
    fun `getSession returns saved session`() = runTest {
        val session = SplitSession(
            id = "test-2",
            query = "Compare models",
            timestamp = 1000L
        )
        repository.saveSession(session)
        val retrieved = repository.getSession("test-2")
        assertNotNull(retrieved)
        assertEquals("Compare models", retrieved!!.query)
    }

    @Test
    fun `getSession returns null for nonexistent`() = runTest {
        val result = repository.getSession("nonexistent")
        assertNull(result)
    }

    @Test
    fun `deleteSession removes session`() = runTest {
        val session = SplitSession(id = "test-3", query = "Delete me")
        repository.saveSession(session)
        repository.deleteSession("test-3")
        val sessions = repository.getAllSessions().first()
        assertTrue(sessions.isEmpty())
    }

    @Test
    fun `deleteSession does not affect other sessions`() = runTest {
        val session1 = SplitSession(id = "s1", query = "First")
        val session2 = SplitSession(id = "s2", query = "Second")
        repository.saveSession(session1)
        repository.saveSession(session2)
        repository.deleteSession("s1")
        val sessions = repository.getAllSessions().first()
        assertEquals(1, sessions.size)
        assertEquals("s2", sessions.first().id)
    }

    @Test
    fun `getAllSessions sorted by timestamp descending`() = runTest {
        val older = SplitSession(id = "old", query = "Older", timestamp = 1000L)
        val newer = SplitSession(id = "new", query = "Newer", timestamp = 2000L)
        repository.saveSession(older)
        repository.saveSession(newer)
        val sessions = repository.getAllSessions().first()
        assertEquals("new", sessions.first().id)
        assertEquals("old", sessions.last().id)
    }

    @Test
    fun `saveSession preserves results`() = runTest {
        val results = listOf(
            SplitResult(
                providerId = "openai",
                providerName = "OpenAI",
                modelName = "gpt-4o",
                response = "Hello!",
                latencyMs = 500,
                tokensUsed = 10,
                rating = 5
            ),
            SplitResult(
                providerId = "anthropic",
                providerName = "Anthropic",
                modelName = "claude-sonnet-4-20250514",
                response = "Hi there!",
                latencyMs = 600,
                tokensUsed = 12,
                rating = 4
            )
        )
        val session = SplitSession(
            id = "test-results",
            query = "Compare AI",
            results = results,
            timestamp = System.currentTimeMillis()
        )
        repository.saveSession(session)
        val retrieved = repository.getSession("test-results")!!
        assertEquals(2, retrieved.results.size)
        assertEquals("OpenAI", retrieved.results[0].providerName)
        assertEquals("Anthropic", retrieved.results[1].providerName)
    }

    @Test
    fun `saveSession with selectedWinner`() = runTest {
        val session = SplitSession(
            id = "winner-test",
            query = "Best AI?",
            selectedWinner = "anthropic",
            timestamp = System.currentTimeMillis()
        )
        repository.saveSession(session)
        val retrieved = repository.getSession("winner-test")!!
        assertEquals("anthropic", retrieved.selectedWinner)
    }

    @Test
    fun `saveSession overwrites existing with same id`() = runTest {
        val session1 = SplitSession(id = "same-id", query = "First version")
        val session2 = SplitSession(id = "same-id", query = "Second version")
        repository.saveSession(session1)
        repository.saveSession(session2)
        val sessions = repository.getAllSessions().first()
        assertEquals(1, sessions.size)
        assertEquals("Second version", sessions.first().query)
    }

    @Test
    fun `ComparisonMode has correct counts`() = runTest {
        assertEquals(2, ComparisonMode.TWO.count)
        assertEquals(3, ComparisonMode.THREE.count)
        assertEquals(4, ComparisonMode.FOUR.count)
    }

    @Test
    fun `SplitResult default values`() = runTest {
        val result = SplitResult(
            providerId = "test",
            providerName = "Test",
            modelName = "test-model"
        )
        assertEquals("", result.response)
        assertFalse(result.isLoading)
        assertNull(result.error)
        assertEquals(0L, result.latencyMs)
        assertEquals(0, result.tokensUsed)
        assertEquals(0, result.rating)
    }
}
