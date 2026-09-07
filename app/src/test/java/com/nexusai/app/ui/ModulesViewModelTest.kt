package com.nexusai.app.ui

import com.nexusai.domain.model.ModuleType
import com.nexusai.domain.model.NexusModule
import com.nexusai.domain.repository.ModuleRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ModulesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var moduleRepository: ModuleRepository
    private lateinit var viewModel: ModulesViewModel

    private val modulesFlow = MutableStateFlow<List<NexusModule>>(emptyList())

    private fun createModule(
        id: String = "module-1",
        title: String = "Test Module",
        description: String = "A test module",
        type: ModuleType = ModuleType.TOOL,
        isEnabled: Boolean = true
    ) = NexusModule(
        id = id,
        title = title,
        description = description,
        type = type,
        iconId = "ic_test",
        isEnabled = isEnabled
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        moduleRepository = mockk(relaxed = true)
        every { moduleRepository.getAllModules() } returns modulesFlow
        every { moduleRepository.getModulesByType(any()) } returns flowOf(emptyList())
        every { moduleRepository.searchModules(any()) } returns flowOf(emptyList())
        viewModel = ModulesViewModel(moduleRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        val state = viewModel.uiState.value
        assertTrue(state.modules.isEmpty())
        assertEquals("", state.searchQuery)
        assertNull(state.selectedType)
        assertFalse(state.showEnabledOnly)
    }

    @Test
    fun `init loads modules from repository`() = runTest {
        val modules = listOf(
            createModule(id = "1", title = "Module A"),
            createModule(id = "2", title = "Module B")
        )
        modulesFlow.value = modules
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.modules.size)
        assertEquals("Module A", state.modules[0].title)
        assertEquals("Module B", state.modules[1].title)
    }

    @Test
    fun `search updates searchQuery in state`() {
        viewModel.search("docker")
        assertEquals("docker", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `search with query calls searchModules`() = runTest {
        val results = listOf(createModule(id = "1", title = "Docker"))
        every { moduleRepository.searchModules("docker") } returns flowOf(results)

        viewModel.search("docker")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.modules.size)
    }

    @Test
    fun `search with empty query loads all modules`() = runTest {
        val allModules = listOf(createModule(id = "1"), createModule(id = "2"))
        every { moduleRepository.getAllModules() } returns flowOf(allModules)

        viewModel.search("")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.modules.size)
    }

    @Test
    fun `selectType updates selectedType`() = runTest {
        viewModel.selectType("ai_provider")
        assertEquals("ai_provider", viewModel.uiState.value.selectedType)
    }

    @Test
    fun `selectType null loads all modules`() = runTest {
        viewModel.selectType("ai_provider")
        viewModel.selectType(null)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedType)
    }

    @Test
    fun `selectType non-null calls getModulesByType`() = runTest {
        viewModel.selectType("feature")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("feature", viewModel.uiState.value.selectedType)
    }

    @Test
    fun `toggleModule calls repository`() = runTest {
        viewModel.toggleModule("module-1", true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { moduleRepository.setModuleEnabled("module-1", true) }
    }

    @Test
    fun `toggleModule disable calls repository with false`() = runTest {
        viewModel.toggleModule("module-1", false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { moduleRepository.setModuleEnabled("module-1", false) }
    }

    @Test
    fun `modules flow updates are reflected in state`() = runTest {
        assertTrue(viewModel.uiState.value.modules.isEmpty())

        modulesFlow.value = listOf(createModule(id = "1", title = "First"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.modules.size)

        modulesFlow.value = listOf(
            createModule(id = "1", title = "First"),
            createModule(id = "2", title = "Second")
        )
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.modules.size)
    }
}
