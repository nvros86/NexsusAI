package com.nexusai.app.ui

import android.app.Application
import com.nexusai.domain.model.AutomationChain
import com.nexusai.domain.model.ChainRunResult
import com.nexusai.domain.model.ChainStep
import com.nexusai.domain.model.ChainStepResult
import com.nexusai.domain.model.ChainStepType
import com.nexusai.domain.repository.ChainRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.VarargOf
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
class ChainsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockApplication = mockk<Application>(relaxed = true)
    private lateinit var chainRepository: ChainRepository
    private lateinit var viewModel: ChainsViewModel

    private val chainsFlow = MutableStateFlow<List<AutomationChain>>(emptyList())

    private fun createChain(
        id: String = "chain-1",
        name: String = "Test Chain",
        description: String = "A test chain",
        steps: List<ChainStep> = emptyList(),
        isActive: Boolean = true,
        runCount: Int = 0
    ) = AutomationChain(
        id = id,
        name = name,
        description = description,
        steps = steps,
        isActive = isActive,
        runCount = runCount
    )

    private fun createStep(
        id: String = "step-1",
        type: ChainStepType = ChainStepType.TEXT_GENERATION,
        name: String = "Generate text",
        prompt: String = "Write a poem"
    ) = ChainStep(
        id = id,
        type = type,
        name = name,
        prompt = prompt
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        chainRepository = mockk(relaxed = true)
        every { chainRepository.getAllChains() } returns chainsFlow
        every { mockApplication.getString(any()) } answers { "Test string" }
        every { mockApplication.getString(any(), any<VarargOf<*>>()) } answers { "Test string" }
        every { mockApplication.getString(any(), any()) } answers { "Test string" }
        viewModel = ChainsViewModel(mockApplication, chainRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        val state = viewModel.uiState.value
        assertTrue(state.chains.isEmpty())
        assertFalse(state.isRunning)
        assertNull(state.runningChainId)
        assertNull(state.error)
    }

    @Test
    fun `init loads chains from repository`() = runTest {
        val chains = listOf(
            createChain(id = "1", name = "Chain A"),
            createChain(id = "2", name = "Chain B")
        )
        chainsFlow.value = chains
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.chains.size)
        assertEquals("Chain A", state.chains[0].name)
        assertEquals("Chain B", state.chains[1].name)
    }

    @Test
    fun `chains flow updates are reflected in state`() = runTest {
        assertTrue(viewModel.uiState.value.chains.isEmpty())

        chainsFlow.value = listOf(createChain(id = "1", name = "First"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.chains.size)

        chainsFlow.value = listOf(
            createChain(id = "1", name = "First"),
            createChain(id = "2", name = "Second")
        )
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.chains.size)
    }

    @Test
    fun `runChain sets running state`() = runTest {
        val chain = createChain(id = "chain-1")
        coEvery { chainRepository.runChain(any()) } returns ChainRunResult(
            chainId = "chain-1",
            stepResults = emptyList()
        )

        viewModel.runChain(chain)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRunning)
        assertNull(state.runningChainId)
    }

    @Test
    fun `runChain calls repository`() = runTest {
        val chain = createChain(id = "chain-1")
        coEvery { chainRepository.runChain(any()) } returns ChainRunResult(
            chainId = "chain-1",
            stepResults = emptyList()
        )

        viewModel.runChain(chain)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { chainRepository.runChain(chain) }
    }

    @Test
    fun `runChain with steps calls repository`() = runTest {
        val steps = listOf(
            createStep(id = "s1", name = "Step 1", prompt = "Hello"),
            createStep(id = "s2", name = "Step 2", prompt = "World")
        )
        val chain = createChain(id = "chain-1", steps = steps)
        coEvery { chainRepository.runChain(any()) } returns ChainRunResult(
            chainId = "chain-1",
            stepResults = listOf(
                ChainStepResult(stepId = "s1", stepName = "Step 1", input = "Hello", output = "Result 1"),
                ChainStepResult(stepId = "s2", stepName = "Step 2", input = "World", output = "Result 2")
            )
        )

        viewModel.runChain(chain)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { chainRepository.runChain(chain) }
        val state = viewModel.uiState.value
        assertFalse(state.isRunning)
    }

    @Test
    fun `runChain sets error on failure`() = runTest {
        val chain = createChain(id = "chain-1")
        coEvery { chainRepository.runChain(any()) } throws RuntimeException("Execution failed")

        viewModel.runChain(chain)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Execution failed"))
        assertFalse(state.isRunning)
        assertNull(state.runningChainId)
    }

    @Test
    fun `runChain sets generic error on failure with null message`() = runTest {
        val chain = createChain(id = "chain-1")
        coEvery { chainRepository.runChain(any()) } throws RuntimeException()

        viewModel.runChain(chain)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Неизвестная ошибка"))
    }

    @Test
    fun `runChain clears error before running`() = runTest {
        val chain = createChain(id = "chain-1")
        coEvery { chainRepository.runChain(any()) } throws RuntimeException("First error")

        viewModel.runChain(chain)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)

        coEvery { chainRepository.runChain(any()) } returns ChainRunResult(
            chainId = "chain-1", stepResults = emptyList()
        )

        viewModel.runChain(chain)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `deleteChain calls repository`() = runTest {
        viewModel.deleteChain("chain-1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { chainRepository.deleteChain("chain-1") }
    }

    @Test
    fun `deleteChain with different id calls repository`() = runTest {
        viewModel.deleteChain("chain-42")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { chainRepository.deleteChain("chain-42") }
    }

    @Test
    fun `clearError resets error to null`() = runTest {
        val chain = createChain(id = "chain-1")
        coEvery { chainRepository.runChain(any()) } throws RuntimeException("error")
        viewModel.runChain(chain)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `clearError on null error does nothing`() {
        assertNull(viewModel.uiState.value.error)
        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `AutomationChain has correct defaults`() {
        val chain = AutomationChain(
            id = "1",
            name = "Test",
            description = "Desc"
        )
        assertTrue(chain.steps.isEmpty())
        assertTrue(chain.isActive)
        assertEquals(0, chain.runCount)
        assertNotNull(chain.createdAt)
        assertNotNull(chain.updatedAt)
    }

    @Test
    fun `ChainStep has correct defaults`() {
        val step = ChainStep(
            id = "1",
            type = ChainStepType.TEXT_GENERATION,
            name = "Step",
            prompt = "Do something"
        )
        assertNull(step.providerType)
        assertEquals("", step.model)
        assertEquals("", step.outputKey)
        assertTrue(step.isEnabled)
    }

    @Test
    fun `ChainStepType has all types`() {
        val types = ChainStepType.entries
        assertEquals(7, types.size)
        assertTrue(types.contains(ChainStepType.TEXT_GENERATION))
        assertTrue(types.contains(ChainStepType.IMAGE_GENERATION))
        assertTrue(types.contains(ChainStepType.VIDEO_GENERATION))
        assertTrue(types.contains(ChainStepType.CODE_GENERATION))
        assertTrue(types.contains(ChainStepType.SUMMARIZATION))
        assertTrue(types.contains(ChainStepType.TRANSLATION))
        assertTrue(types.contains(ChainStepType.CUSTOM))
    }
}
