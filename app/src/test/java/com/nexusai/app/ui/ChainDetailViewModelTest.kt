package com.nexusai.app.ui

import androidx.lifecycle.SavedStateHandle
import com.nexusai.domain.model.AutomationChain
import com.nexusai.domain.model.ChainRunResult
import com.nexusai.domain.model.ChainStep
import com.nexusai.domain.model.ChainStepType
import com.nexusai.domain.repository.ChainRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
class ChainDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var chainRepository: ChainRepository
    private lateinit var viewModel: ChainDetailViewModel

    private fun createStep(
        id: String = "step-1",
        name: String = "Test Step",
        prompt: String = "Test prompt",
        type: ChainStepType = ChainStepType.TEXT_GENERATION,
        outputKey: String = "step_1",
        isEnabled: Boolean = true
    ) = ChainStep(
        id = id,
        type = type,
        name = name,
        prompt = prompt,
        outputKey = outputKey,
        isEnabled = isEnabled
    )

    private fun createChain(
        id: String = "chain-1",
        name: String = "Test Chain",
        description: String = "Description",
        steps: List<ChainStep> = emptyList()
    ) = AutomationChain(
        id = id,
        name = name,
        description = description,
        steps = steps
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        chainRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(chainId: String = "new"): ChainDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("chainId" to chainId))
        return ChainDetailViewModel(savedStateHandle, chainRepository)
    }

    @Test
    fun `initial state has default values for new chain`() = runTest {
        viewModel = createViewModel("new")
        val state = viewModel.uiState.value

        assertEquals("", state.chainName)
        assertEquals("", state.chainDescription)
        assertTrue(state.steps.isEmpty())
        assertFalse(state.isRunning)
        assertNull(state.lastResult)
        assertEquals("", state.newStepName)
        assertEquals("", state.newStepPrompt)
        assertEquals(ChainStepType.TEXT_GENERATION, state.newStepType)
    }

    @Test
    fun `init loads existing chain by id`() = runTest {
        val steps = listOf(createStep(id = "s1", name = "Step 1"), createStep(id = "s2", name = "Step 2"))
        val chain = createChain(id = "chain-1", name = "Loaded Chain", description = "Loaded Desc", steps = steps)
        coEvery { chainRepository.getChainById("chain-1") } returns chain

        viewModel = createViewModel("chain-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Loaded Chain", state.chainName)
        assertEquals("Loaded Desc", state.chainDescription)
        assertEquals(2, state.steps.size)
        assertEquals("Step 1", state.steps[0].name)
        assertEquals("Step 2", state.steps[1].name)
    }

    @Test
    fun `init with nonexistent chain keeps defaults`() = runTest {
        coEvery { chainRepository.getChainById("missing") } returns null

        viewModel = createViewModel("missing")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.chainName)
        assertTrue(viewModel.uiState.value.steps.isEmpty())
    }

    @Test
    fun `setChainName updates chainName`() {
        viewModel = createViewModel()
        viewModel.setChainName("My Chain")
        assertEquals("My Chain", viewModel.uiState.value.chainName)
    }

    @Test
    fun `setChainDescription updates chainDescription`() {
        viewModel = createViewModel()
        viewModel.setChainDescription("A description")
        assertEquals("A description", viewModel.uiState.value.chainDescription)
    }

    @Test
    fun `setNewStepName updates newStepName`() {
        viewModel = createViewModel()
        viewModel.setNewStepName("New Step")
        assertEquals("New Step", viewModel.uiState.value.newStepName)
    }

    @Test
    fun `setNewStepPrompt updates newStepPrompt`() {
        viewModel = createViewModel()
        viewModel.setNewStepPrompt("Do something")
        assertEquals("Do something", viewModel.uiState.value.newStepPrompt)
    }

    @Test
    fun `setNewStepType updates newStepType`() {
        viewModel = createViewModel()
        viewModel.setNewStepType(ChainStepType.IMAGE_GENERATION)
        assertEquals(ChainStepType.IMAGE_GENERATION, viewModel.uiState.value.newStepType)
    }

    @Test
    fun `addStep adds step and clears form`() = runTest {
        viewModel = createViewModel()

        viewModel.setNewStepName("Step One")
        viewModel.setNewStepPrompt("Generate text")
        viewModel.setNewStepType(ChainStepType.TEXT_GENERATION)
        viewModel.addStep()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.steps.size)
        assertEquals("Step One", state.steps[0].name)
        assertEquals("Generate text", state.steps[0].prompt)
        assertEquals(ChainStepType.TEXT_GENERATION, state.steps[0].type)
        assertEquals("", state.newStepName)
        assertEquals("", state.newStepPrompt)
        coVerify { chainRepository.saveChain(any()) }
    }

    @Test
    fun `addStep does nothing when name is blank`() = runTest {
        viewModel = createViewModel()
        viewModel.setNewStepName("  ")
        viewModel.setNewStepPrompt("Prompt")
        viewModel.addStep()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.steps.isEmpty())
    }

    @Test
    fun `addStep does nothing when prompt is blank`() = runTest {
        viewModel = createViewModel()
        viewModel.setNewStepName("Name")
        viewModel.setNewStepPrompt("  ")
        viewModel.addStep()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.steps.isEmpty())
    }

    @Test
    fun `addStep generates outputKey based on step count`() = runTest {
        viewModel = createViewModel()
        viewModel.setNewStepName("First")
        viewModel.setNewStepPrompt("Prompt 1")
        viewModel.addStep()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("step_1", viewModel.uiState.value.steps[0].outputKey)

        viewModel.setNewStepName("Second")
        viewModel.setNewStepPrompt("Prompt 2")
        viewModel.addStep()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.steps.size)
        assertEquals("step_2", viewModel.uiState.value.steps[1].outputKey)
    }

    @Test
    fun `deleteStep removes step by id`() = runTest {
        viewModel = createViewModel()
        viewModel.setNewStepName("Step 1")
        viewModel.setNewStepPrompt("Prompt 1")
        viewModel.addStep()
        testDispatcher.scheduler.advanceUntilIdle()

        val stepId = viewModel.uiState.value.steps[0].id
        viewModel.deleteStep(stepId)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.steps.isEmpty())
        coVerify { chainRepository.saveChain(any()) }
    }

    @Test
    fun `deleteStep does not affect other steps`() = runTest {
        viewModel = createViewModel()
        viewModel.setNewStepName("Step 1")
        viewModel.setNewStepPrompt("Prompt 1")
        viewModel.addStep()
        viewModel.setNewStepName("Step 2")
        viewModel.setNewStepPrompt("Prompt 2")
        viewModel.addStep()
        testDispatcher.scheduler.advanceUntilIdle()

        val step1Id = viewModel.uiState.value.steps[0].id
        viewModel.deleteStep(step1Id)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.steps.size)
        assertEquals("Step 2", viewModel.uiState.value.steps[0].name)
    }

    @Test
    fun `toggleStep toggles isEnabled flag`() = runTest {
        viewModel = createViewModel()
        viewModel.setNewStepName("Step 1")
        viewModel.setNewStepPrompt("Prompt 1")
        viewModel.addStep()
        testDispatcher.scheduler.advanceUntilIdle()

        val stepId = viewModel.uiState.value.steps[0].id
        assertTrue(viewModel.uiState.value.steps[0].isEnabled)

        viewModel.toggleStep(stepId)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.steps[0].isEnabled)

        viewModel.toggleStep(stepId)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.steps[0].isEnabled)
    }

    @Test
    fun `runChain does nothing when steps are empty`() = runTest {
        viewModel = createViewModel()
        viewModel.runChain()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRunning)
        assertNull(viewModel.uiState.value.lastResult)
    }

    @Test
    fun `runChain sets isRunning true then false with result`() = runTest {
        viewModel = createViewModel()
        viewModel.setChainName("My Chain")
        viewModel.setNewStepName("Step 1")
        viewModel.setNewStepPrompt("Prompt")
        viewModel.addStep()
        testDispatcher.scheduler.advanceUntilIdle()

        val result = ChainRunResult(
            chainId = "new",
            stepResults = emptyList(),
            isError = false
        )
        coEvery { chainRepository.runChain(any()) } returns result

        viewModel.runChain()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRunning)
        assertNotNull(state.lastResult)
        assertFalse(state.lastResult!!.isError)
    }

    @Test
    fun `runChain handles error from repository`() = runTest {
        viewModel = createViewModel()
        viewModel.setNewStepName("Step 1")
        viewModel.setNewStepPrompt("Prompt")
        viewModel.addStep()
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { chainRepository.runChain(any()) } throws RuntimeException("Run failed")

        viewModel.runChain()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRunning)
        assertNotNull(state.lastResult)
        assertTrue(state.lastResult!!.isError)
        assertEquals("Run failed", state.lastResult!!.errorMessage)
    }

    @Test
    fun `runChain constructs chain with correct name`() = runTest {
        viewModel = createViewModel("chain-123")
        viewModel.setChainName("Custom Name")
        viewModel.setChainDescription("Desc")
        viewModel.setNewStepName("Step 1")
        viewModel.setNewStepPrompt("Prompt")
        viewModel.addStep()
        testDispatcher.scheduler.advanceUntilIdle()

        val result = ChainRunResult(chainId = "chain-123", stepResults = emptyList())
        coEvery { chainRepository.runChain(any()) } returns result

        viewModel.runChain()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            chainRepository.runChain(match {
                it.id == "chain-123" && it.name == "Custom Name" && it.description == "Desc"
            })
        }
    }

    @Test
    fun `addStep multiple times accumulates steps`() = runTest {
        viewModel = createViewModel()

        for (i in 1..5) {
            viewModel.setNewStepName("Step $i")
            viewModel.setNewStepPrompt("Prompt $i")
            viewModel.addStep()
            testDispatcher.scheduler.advanceUntilIdle()
        }

        assertEquals(5, viewModel.uiState.value.steps.size)
        for (i in 1..5) {
            assertEquals("Step $i", viewModel.uiState.value.steps[i - 1].name)
        }
    }

    @Test
    fun `saveCurrentChain called after addStep`() = runTest {
        viewModel = createViewModel("chain-save")
        viewModel.setNewStepName("Step")
        viewModel.setNewStepPrompt("Prompt")
        viewModel.addStep()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            chainRepository.saveChain(match {
                it.id == "chain-save" && it.steps.size == 1
            })
        }
    }

    @Test
    fun `saveCurrentChain called after deleteStep`() = runTest {
        viewModel = createViewModel("chain-del")
        viewModel.setNewStepName("Step")
        viewModel.setNewStepPrompt("Prompt")
        viewModel.addStep()
        testDispatcher.scheduler.advanceUntilIdle()

        val stepId = viewModel.uiState.value.steps[0].id
        viewModel.deleteStep(stepId)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            chainRepository.saveChain(match {
                it.id == "chain-del" && it.steps.isEmpty()
            })
        }
    }
}
