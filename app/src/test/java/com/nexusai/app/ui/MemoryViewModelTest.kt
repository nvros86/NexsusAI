package com.nexusai.app.ui

import com.nexusai.data.common.AppDataManager
import com.nexusai.domain.model.MemoryEntry
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MemoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var appDataManager: AppDataManager
    private lateinit var viewModel: MemoryViewModel

    private val entriesFlow = MutableStateFlow<List<MemoryEntry>>(emptyList())

    private fun createEntry(
        id: String = "entry-1",
        key: String = "test-key",
        value: String = "test-value",
        isImportant: Boolean = false
    ) = MemoryEntry(
        id = id,
        key = key,
        value = value,
        isImportant = isImportant
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        appDataManager = mockk(relaxed = true)
        every { appDataManager.memoryEntries } returns entriesFlow
        viewModel = MemoryViewModel(appDataManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty entries and default values`() = runTest {
        val state = viewModel.uiState.value
        assertTrue(state.entries.isEmpty())
        assertEquals("", state.searchQuery)
        assertEquals("", state.newKey)
        assertEquals("", state.newValue)
        assertNull(state.error)
    }

    @Test
    fun `init loads entries from appDataManager`() = runTest {
        val entries = listOf(
            createEntry(id = "1", key = "name", value = "Alice"),
            createEntry(id = "2", key = "color", value = "blue")
        )
        entriesFlow.value = entries
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.entries.size)
        assertEquals("name", state.entries[0].key)
        assertEquals("color", state.entries[1].key)
    }

    @Test
    fun `setSearchQuery updates state`() {
        viewModel.setSearchQuery("query text")
        assertEquals("query text", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `setNewKey updates state`() {
        viewModel.setNewKey("my-key")
        assertEquals("my-key", viewModel.uiState.value.newKey)
    }

    @Test
    fun `setNewValue updates state`() {
        viewModel.setNewValue("my-value")
        assertEquals("my-value", viewModel.uiState.value.newValue)
    }

    @Test
    fun `addEntry adds entry and clears form fields`() = runTest {
        viewModel.setNewKey("temperature")
        viewModel.setNewValue("25C")

        viewModel.addEntry()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { appDataManager.addMemoryEntry(match {
            it.key == "temperature" && it.value == "25C"
        }) }

        val state = viewModel.uiState.value
        assertEquals("", state.newKey)
        assertEquals("", state.newValue)
    }

    @Test
    fun `addEntry does nothing when key is blank`() = runTest {
        viewModel.setNewKey("  ")
        viewModel.setNewValue("some value")

        viewModel.addEntry()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { appDataManager.addMemoryEntry(any()) }
    }

    @Test
    fun `addEntry does nothing when value is blank`() = runTest {
        viewModel.setNewKey("some key")
        viewModel.setNewValue("  ")

        viewModel.addEntry()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { appDataManager.addMemoryEntry(any()) }
    }

    @Test
    fun `addEntry does nothing when key and value are empty`() = runTest {
        viewModel.addEntry()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { appDataManager.addMemoryEntry(any()) }
    }

    @Test
    fun `addEntry sets error on failure`() = runTest {
        viewModel.setNewKey("key")
        viewModel.setNewValue("value")
        coEvery { appDataManager.addMemoryEntry(any()) } throws RuntimeException("DB error")

        viewModel.addEntry()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.isNotBlank())
    }

    @Test
    fun `toggleImportant toggles isImportant from false to true`() = runTest {
        val entry = createEntry(id = "entry-1", isImportant = false)
        entriesFlow.value = listOf(entry)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleImportant("entry-1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { appDataManager.updateMemoryEntry(match {
            it.id == "entry-1" && it.isImportant
        }) }
    }

    @Test
    fun `toggleImportant toggles isImportant from true to false`() = runTest {
        val entry = createEntry(id = "entry-1", isImportant = true)
        entriesFlow.value = listOf(entry)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleImportant("entry-1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { appDataManager.updateMemoryEntry(match {
            it.id == "entry-1" && !it.isImportant
        }) }
    }

    @Test
    fun `toggleImportant does nothing for nonexistent entry`() = runTest {
        entriesFlow.value = emptyList()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleImportant("nonexistent")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { appDataManager.updateMemoryEntry(any()) }
    }

    @Test
    fun `toggleImportant sets error on failure`() = runTest {
        val entry = createEntry(id = "entry-1", isImportant = false)
        entriesFlow.value = listOf(entry)
        testDispatcher.scheduler.advanceUntilIdle()
        coEvery { appDataManager.updateMemoryEntry(any()) } throws RuntimeException("Update error")

        viewModel.toggleImportant("entry-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.isNotBlank())
    }

    @Test
    fun `deleteEntry calls removeMemoryEntry on appDataManager`() = runTest {
        viewModel.deleteEntry("entry-1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { appDataManager.removeMemoryEntry("entry-1") }
    }

    @Test
    fun `deleteEntry sets error on failure`() = runTest {
        coEvery { appDataManager.removeMemoryEntry(any()) } throws RuntimeException("Delete error")

        viewModel.deleteEntry("entry-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.isNotBlank())
    }

    @Test
    fun `clearAll calls clearMemory on appDataManager`() = runTest {
        viewModel.clearAll()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { appDataManager.clearMemory() }
    }

    @Test
    fun `clearAll sets error on failure`() = runTest {
        coEvery { appDataManager.clearMemory() } throws RuntimeException("Clear error")

        viewModel.clearAll()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.isNotBlank())
    }

    @Test
    fun `clearError resets error to null`() = runTest {
        coEvery { appDataManager.addMemoryEntry(any()) } throws RuntimeException("error")
        viewModel.setNewKey("key")
        viewModel.setNewValue("value")
        viewModel.addEntry()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `entries flow updates are reflected in state`() = runTest {
        assertTrue(viewModel.uiState.value.entries.isEmpty())

        entriesFlow.value = listOf(createEntry(id = "1", key = "first"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.entries.size)

        entriesFlow.value = listOf(
            createEntry(id = "1", key = "first"),
            createEntry(id = "2", key = "second")
        )
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.entries.size)
    }

    @Test
    fun `form fields can be set independently`() {
        viewModel.setNewKey("key-only")
        assertEquals("key-only", viewModel.uiState.value.newKey)
        assertEquals("", viewModel.uiState.value.newValue)

        viewModel.setNewValue("value-only")
        assertEquals("key-only", viewModel.uiState.value.newKey)
        assertEquals("value-only", viewModel.uiState.value.newValue)
    }

    @Test
    fun `multiple entries can be toggled independently`() = runTest {
        val entries = listOf(
            createEntry(id = "1", key = "a", isImportant = false),
            createEntry(id = "2", key = "b", isImportant = true)
        )
        entriesFlow.value = entries
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleImportant("1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { appDataManager.updateMemoryEntry(match {
            it.id == "1" && it.isImportant
        }) }

        viewModel.toggleImportant("2")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { appDataManager.updateMemoryEntry(match {
            it.id == "2" && !it.isImportant
        }) }
    }
}
