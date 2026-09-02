package com.nexusai.feature.settings.viewmodel

import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.ProviderType
import com.nexusai.core.preferences.AppPreferencesRepository
import com.nexusai.domain.repository.AIProviderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AIProviderRepository
    private lateinit var appPreferences: AppPreferencesRepository
    private lateinit var viewModel: SettingsViewModel

    private fun createProvider(
        id: String = "1",
        name: String = "Test Provider",
        type: ProviderType = ProviderType.OPENAI,
        isFavorite: Boolean = false
    ) = AIProviderConfig(
        id = id,
        name = name,
        type = type,
        baseUrl = "https://api.openai.com/v1",
        apiKey = "test-key",
        defaultModel = "gpt-4",
        maxTokens = 4096,
        temperature = 0.7f,
        isFavorite = isFavorite
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        appPreferences = mockk(relaxed = true)
        every { appPreferences.fontScale } returns flowOf(1)
        every { appPreferences.isHighContrast } returns flowOf(false)
        every { appPreferences.isIncognitoMode } returns flowOf(false)
        every { appPreferences.isHapticEnabled } returns flowOf(true)
        every { appPreferences.isAppLockEnabled } returns flowOf(false)
        every { appPreferences.isDarkMode } returns flowOf(true)
        coEvery { repository.getAllProviders() } returns flowOf(emptyList())
        viewModel = SettingsViewModel(repository, appPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty providers`() = runTest {
        val state = viewModel.state.value
        assertTrue(state.providers.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.showAddDialog)
        assertNull(state.editingProvider)
    }

    @Test
    fun `showAddDialog sets showAddDialog true and editingProvider null`() {
        viewModel.showAddDialog()
        val state = viewModel.state.value
        assertTrue(state.showAddDialog)
        assertNull(state.editingProvider)
    }

    @Test
    fun `showEditDialog sets editingProvider`() {
        val provider = createProvider()
        viewModel.showEditDialog(provider)
        val state = viewModel.state.value
        assertTrue(state.showAddDialog)
        assertEquals(provider, state.editingProvider)
    }

    @Test
    fun `dismissDialog resets dialog state`() {
        viewModel.showAddDialog()
        viewModel.dismissDialog()
        val state = viewModel.state.value
        assertFalse(state.showAddDialog)
        assertNull(state.editingProvider)
    }

    @Test
    fun `deleteProvider calls repository`() = runTest {
        viewModel.deleteProvider("1")
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { repository.deleteProvider("1") }
    }

    @Test
    fun `toggleFavorite updates provider isFavorite`() = runTest {
        val provider = createProvider(isFavorite = false)
        coEvery { repository.getAllProviders() } returns flowOf(listOf(provider))
        val vm = SettingsViewModel(repository, appPreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.toggleFavorite("1")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedSlot = slot<AIProviderConfig>()
        coVerify { repository.updateProvider(capture(updatedSlot)) }
        assertTrue(updatedSlot.captured.isFavorite)
    }

    @Test
    fun `saveProvider creates new provider when not editing`() = runTest {
        viewModel.showAddDialog()
        viewModel.saveProvider(
            name = "New Provider",
            type = ProviderType.ANTHROPIC,
            apiKey = "key",
            baseUrl = "https://api.anthropic.com",
            defaultModel = "claude-3",
            maxTokens = 8192,
            temperature = 0.5f
        )
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.addProvider(any()) }
    }

    @Test
    fun `saveProvider updates existing provider when editing`() = runTest {
        val existing = createProvider()
        coEvery { repository.getAllProviders() } returns flowOf(listOf(existing))
        val vm = SettingsViewModel(repository, appPreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.showEditDialog(existing)
        vm.saveProvider(
            name = "Updated Name",
            type = ProviderType.OPENAI,
            apiKey = "new-key",
            baseUrl = "https://api.openai.com/v1",
            defaultModel = "gpt-4o",
            maxTokens = 4096,
            temperature = 0.7f
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedSlot = slot<AIProviderConfig>()
        coVerify { repository.updateProvider(capture(updatedSlot)) }
        assertEquals("Updated Name", updatedSlot.captured.name)
    }
}
