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
    fun `getSession returns null for nonexistent`() = runTest {
        val result = repository.getSession("nonexistent")
        assertNull(result)
    }

    @Test
    fun `ComparisonMode has correct counts`() {
        assertEquals(2, ComparisonMode.TWO.count)
        assertEquals(3, ComparisonMode.THREE.count)
        assertEquals(4, ComparisonMode.FOUR.count)
    }

    @Test
    fun `SplitResult default values`() {
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

    @Test
    fun `SplitSession default values`() {
        val session = SplitSession(
            id = "test",
            query = "Hello"
        )
        assertTrue(session.results.isEmpty())
        assertNull(session.selectedWinner)
    }

    @Test
    fun `SplitResult with all fields`() {
        val result = SplitResult(
            providerId = "openai",
            providerName = "OpenAI",
            modelName = "gpt-4o",
            response = "Hello!",
            isLoading = false,
            error = null,
            latencyMs = 500,
            tokensUsed = 10,
            rating = 5
        )
        assertEquals("openai", result.providerId)
        assertEquals(500L, result.latencyMs)
        assertEquals(5, result.rating)
    }

    @Test
    fun `ComparisonMode display names`() {
        assertEquals("2 AI", ComparisonMode.TWO.displayName)
        assertEquals("3 AI", ComparisonMode.THREE.displayName)
        assertEquals("4 AI", ComparisonMode.FOUR.displayName)
    }
}
