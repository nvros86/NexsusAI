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
class PluginRepositoryImplTest {

    private lateinit var repository: PluginRepositoryImpl
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
        repository = PluginRepositoryImpl()
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `getAllPlugins returns 6 built-in plugins`() = runTest {
        val plugins = repository.getAllPlugins().first()
        assertEquals(6, plugins.size)
    }

    @Test
    fun `getAllPlugins contains Git plugin`() = runTest {
        val plugins = repository.getAllPlugins().first()
        val gitPlugin = plugins.find { it.id == "git_plugin" }
        assertNotNull(gitPlugin)
        assertEquals("Git Plugin", gitPlugin?.name)
    }

    @Test
    fun `getAllPlugins contains Docker plugin`() = runTest {
        val plugins = repository.getAllPlugins().first()
        val dockerPlugin = plugins.find { it.id == "docker_plugin" }
        assertNotNull(dockerPlugin)
        assertEquals("Docker Plugin", dockerPlugin?.name)
    }

    @Test
    fun `getAllPlugins contains Firebase plugin`() = runTest {
        val plugins = repository.getAllPlugins().first()
        val firebasePlugin = plugins.find { it.id == "firebase_plugin" }
        assertNotNull(firebasePlugin)
        assertEquals("Firebase Plugin", firebasePlugin?.name)
    }

    @Test
    fun `getPluginById returns plugin`() = runTest {
        val plugin = repository.getPluginById("git_plugin")
        assertNotNull(plugin)
        assertEquals("Git Plugin", plugin?.name)
    }

    @Test
    fun `getPluginById returns null for invalid id`() = runTest {
        val plugin = repository.getPluginById("invalid_id")
        assertNull(plugin)
    }

    @Test
    fun `disablePlugin disables plugin`() = runTest {
        repository.disablePlugin("git_plugin")
        val plugin = repository.getPluginById("git_plugin")
        assertFalse(plugin?.isEnabled ?: true)
    }

    @Test
    fun `enablePlugin enables plugin`() = runTest {
        repository.disablePlugin("git_plugin")
        repository.enablePlugin("git_plugin")
        val plugin = repository.getPluginById("git_plugin")
        assertTrue(plugin?.isEnabled ?: false)
    }

    @Test
    fun `getEnabledPlugins returns only enabled plugins`() = runTest {
        repository.disablePlugin("git_plugin")
        val enabled = repository.getEnabledPlugins().first()
        assertTrue(enabled.none { it.id == "git_plugin" })
    }

    @Test
    fun `getPluginCommands for git returns 5 commands`() = runTest {
        val commands = repository.getPluginCommands("git_plugin")
        assertEquals(5, commands.size)
    }

    @Test
    fun `getPluginCommands for docker returns 5 commands`() = runTest {
        val commands = repository.getPluginCommands("docker_plugin")
        assertEquals(5, commands.size)
    }

    @Test
    fun `getPluginCommands for firebase returns 4 commands`() = runTest {
        val commands = repository.getPluginCommands("firebase_plugin")
        assertEquals(4, commands.size)
    }

    @Test
    fun `getPluginCommands for invalid plugin returns empty`() = runTest {
        val commands = repository.getPluginCommands("invalid_plugin")
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `all plugins are built-in`() = runTest {
        val plugins = repository.getAllPlugins().first()
        plugins.forEach { plugin ->
            assertTrue(plugin.isBuiltIn)
        }
    }

    @Test
    fun `all plugins are installed`() = runTest {
        val plugins = repository.getAllPlugins().first()
        plugins.forEach { plugin ->
            assertTrue(plugin.isInstalled)
        }
    }

    @Test
    fun `installPlugin adds new plugin`() = runTest {
        val newPlugin = com.nexusai.domain.model.NexsusPlugin(
            id = "custom_plugin",
            name = "Custom Plugin",
            description = "Custom",
            version = "1.0.0",
            author = "Test",
            iconEmoji = "🔧",
            capabilities = emptyList()
        )
        repository.installPlugin(newPlugin)
        val found = repository.getPluginById("custom_plugin")
        assertNotNull(found)
        assertTrue(found?.isInstalled ?: false)
    }

    @Test
    fun `uninstallPlugin removes plugin`() = runTest {
        val newPlugin = com.nexusai.domain.model.NexsusPlugin(
            id = "to_remove",
            name = "To Remove",
            description = "Remove me",
            version = "1.0.0",
            author = "Test",
            iconEmoji = "🗑️",
            capabilities = emptyList()
        )
        repository.installPlugin(newPlugin)
        repository.uninstallPlugin("to_remove")
        val found = repository.getPluginById("to_remove")
        assertNull(found)
    }

    @Test
    fun `executeCommand for git status returns success`() = runTest {
        val result = repository.executeCommand("git_plugin", "status", "")
        assertTrue(result.success)
        assertTrue(result.output.isNotEmpty())
    }

    @Test
    fun `executeCommand for invalid plugin returns error`() = runTest {
        val result = repository.executeCommand("invalid_plugin", "test", "")
        assertFalse(result.success)
        assertNotNull(result.error)
    }
}
