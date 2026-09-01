package com.nexusai.data.repository

import com.nexusai.domain.model.AutomationChain
import com.nexusai.domain.model.ChainStep
import com.nexusai.domain.model.ChainStepType
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
class ChainRepositoryImplTest {

    private lateinit var repository: ChainRepositoryImpl
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
        val fakeProviderRepo = object : com.nexusai.domain.repository.AIProviderRepository {
            override fun getAllProviders() = kotlinx.coroutines.flow.flowOf(emptyList<com.nexusai.domain.model.AIProviderConfig>())
            override suspend fun getProviderById(id: String) = null
            override suspend fun addProvider(provider: com.nexusai.domain.model.AIProviderConfig) {}
            override suspend fun updateProvider(provider: com.nexusai.domain.model.AIProviderConfig) {}
            override suspend fun deleteProvider(id: String) {}
            override suspend fun getFavoriteProviders() = emptyList<com.nexusai.domain.model.AIProviderConfig>()
        }
        val fakeManager = io.mockk.mockk<com.nexusai.data.ai.AIProviderManager>()
        repository = ChainRepositoryImpl(fakeProviderRepo, fakeManager)
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `getAllChains returns default chains`() = runTest {
        val chains = repository.getAllChains().first()
        assertTrue(chains.isNotEmpty())
    }

    @Test
    fun `getChainById returns chain`() = runTest {
        val chains = repository.getAllChains().first()
        val first = chains.first()
        val found = repository.getChainById(first.id)
        assertNotNull(found)
        assertEquals(first.id, found?.id)
    }

    @Test
    fun `getChainById returns null for invalid id`() = runTest {
        val found = repository.getChainById("invalid_id")
        assertNull(found)
    }

    @Test
    fun `saveChain adds new chain`() = runTest {
        val newChain = AutomationChain(
            id = "test_chain",
            name = "Test Chain",
            description = "Test",
            steps = listOf(
                ChainStep(
                    id = "step1",
                    type = ChainStepType.TEXT_GENERATION,
                    name = "Step 1",
                    prompt = "Test prompt",
                    outputKey = "output"
                )
            )
        )
        repository.saveChain(newChain)
        val found = repository.getChainById("test_chain")
        assertNotNull(found)
        assertEquals("Test Chain", found?.name)
    }

    @Test
    fun `deleteChain removes chain`() = runTest {
        val chains = repository.getAllChains().first()
        val first = chains.first()
        repository.deleteChain(first.id)
        val found = repository.getChainById(first.id)
        assertNull(found)
    }

    @Test
    fun `getAllChains returns flow`() = runTest {
        val chains = repository.getAllChains().first()
        assertNotNull(chains)
        assertTrue(chains is List)
    }

    @Test
    fun `default chains have correct names`() = runTest {
        val chains = repository.getAllChains().first()
        val names = chains.map { it.name }
        assertTrue(names.contains("Видео-пайплайн"))
        assertTrue(names.contains("Блог-пайплайн"))
        assertTrue(names.contains("Код-пайплайн"))
    }

    @Test
    fun `default chains have steps`() = runTest {
        val chains = repository.getAllChains().first()
        chains.forEach { chain ->
            assertTrue(chain.steps.isNotEmpty())
        }
    }

    @Test
    fun `saveChain updates existing chain`() = runTest {
        val chains = repository.getAllChains().first()
        val first = chains.first()
        val updated = first.copy(name = "Updated Name")
        repository.saveChain(updated)
        val found = repository.getChainById(first.id)
        assertEquals("Updated Name", found?.name)
    }

    @Test
    fun `deleteChain does not affect other chains`() = runTest {
        val chains = repository.getAllChains().first()
        val firstId = chains.first().id
        val countBefore = chains.size
        repository.deleteChain(firstId)
        val chainsAfter = repository.getAllChains().first()
        assertEquals(countBefore - 1, chainsAfter.size)
    }
}
