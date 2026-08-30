package com.nexusai.data.repository

import android.content.Context
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
class ModuleRepositoryImplTest {

    private lateinit var repository: ModuleRepositoryImpl
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private val mockContext = mockk<Context>()

    @Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
        every { mockContext.applicationContext } returns mockContext
        repository = ModuleRepositoryImpl(mockContext)
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `getAllModules returns all 11 modules`() = runTest {
        val modules = repository.getAllModules().first()
        assertEquals(11, modules.size)
    }

    @Test
    fun `getAllModules contains marketplace`() = runTest {
        val modules = repository.getAllModules().first()
        assertTrue(modules.any { it.id == "marketplace" })
    }

    @Test
    fun `getAllModules contains ai_chat as required`() = runTest {
        val modules = repository.getAllModules().first()
        val aiChat = modules.first { it.id == "ai_chat" }
        assertTrue(aiChat.isRequired)
    }

    @Test
    fun `getAllModules default enabled state`() = runTest {
        val modules = repository.getAllModules().first()
        modules.forEach { module ->
            assertTrue("Module ${module.id} should be enabled by default", module.isEnabled)
        }
    }

    @Test
    fun `getEnabledModules returns all by default`() = runTest {
        val enabled = repository.getEnabledModules().first()
        assertEquals(11, enabled.size)
    }

    @Test
    fun `getModulesByType AI_PROVIDER returns 3`() = runTest {
        val modules = repository.getModulesByType("AI_PROVIDER").first()
        assertEquals(3, modules.size)
        assertTrue(modules.all { it.type.name == "AI_PROVIDER" })
    }

    @Test
    fun `getModulesByType TOOL returns 3`() = runTest {
        val modules = repository.getModulesByType("TOOL").first()
        assertEquals(3, modules.size)
    }

    @Test
    fun `getModulesByType FEATURE returns 4`() = runTest {
        val modules = repository.getModulesByType("FEATURE").first()
        assertEquals(4, modules.size)
    }

    @Test
    fun `getModulesByType nonexistent returns empty`() = runTest {
        val modules = repository.getModulesByType("NONEXISTENT").first()
        assertTrue(modules.isEmpty())
    }

    @Test
    fun `searchModules by title matches`() = runTest {
        val modules = repository.searchModules("Маркетплейс").first()
        assertEquals(1, modules.size)
        assertEquals("marketplace", modules.first().id)
    }

    @Test
    fun `searchModules by description matches`() = runTest {
        val modules = repository.searchModules("голосовое").first()
        assertTrue(modules.isNotEmpty())
        assertTrue(modules.any { it.id == "voice_mode" })
    }

    @Test
    fun `searchModules by capability matches`() = runTest {
        val modules = repository.searchModules("Streaming").first()
        assertTrue(modules.isNotEmpty())
        assertTrue(modules.any { it.id == "ai_chat" })
    }

    @Test
    fun `searchModules case insensitive`() = runTest {
        val lower = repository.searchModules("чат").first()
        val upper = repository.searchModules("ЧАТ").first()
        assertEquals(lower.size, upper.size)
    }

    @Test
    fun `searchModules no match returns empty`() = runTest {
        val modules = repository.searchModules("несуществующий_модуль_xyz").first()
        assertTrue(modules.isEmpty())
    }

    @Test
    fun `searchModules empty query returns all`() = runTest {
        val modules = repository.searchModules("").first()
        assertEquals(11, modules.size)
    }

    @Test
    fun `isModuleEnabled returns true for existing module`() = runTest {
        assertTrue(repository.isModuleEnabled("marketplace"))
    }

    @Test
    fun `isModuleEnabled returns false for nonexistent module`() = runTest {
        assertFalse(repository.isModuleEnabled("nonexistent"))
    }

    @Test
    fun `setModuleEnabled changes state`() = runTest {
        repository.setModuleEnabled("marketplace", false)
        assertFalse(repository.isModuleEnabled("marketplace"))
    }

    @Test
    fun `setModuleEnabled can re-enable`() = runTest {
        repository.setModuleEnabled("marketplace", false)
        repository.setModuleEnabled("marketplace", true)
        assertTrue(repository.isModuleEnabled("marketplace"))
    }

    @Test
    fun `setModuleEnabled does not affect other modules`() = runTest {
        repository.setModuleEnabled("marketplace", false)
        assertTrue(repository.isModuleEnabled("playground"))
        assertTrue(repository.isModuleEnabled("ai_chat"))
    }

    @Test
    fun `all module types are covered`() = runTest {
        val modules = repository.getAllModules().first()
        val types = modules.map { it.type.name }.toSet()
        assertTrue(types.contains("AI_PROVIDER"))
        assertTrue(types.contains("TOOL"))
        assertTrue(types.contains("FEATURE"))
    }

    @Test
    fun `modules have routes`() = runTest {
        val modules = repository.getAllModules().first()
        modules.forEach { module ->
            assertNotNull("Module ${module.id} should have a route", module.route)
        }
    }

    @Test
    fun `modules have capabilities`() = runTest {
        val modules = repository.getAllModules().first()
        modules.forEach { module ->
            assertTrue("Module ${module.id} should have capabilities", module.capabilities.isNotEmpty())
        }
    }
}
