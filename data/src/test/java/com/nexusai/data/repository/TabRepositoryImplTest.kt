package com.nexusai.data.repository

import com.nexusai.data.local.TabDao
import com.nexusai.data.local.TabEntity
import com.nexusai.domain.model.Message
import com.nexusai.domain.model.MessageRole
import com.nexusai.domain.model.Tab
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TabRepositoryImplTest {

    private lateinit var dao: TabDao
    private lateinit var repository: TabRepositoryImpl

    private fun createEntity(
        id: String = "tab-1",
        title: String = "Test Tab",
        aiProviderId: String? = "provider-1",
        agentId: String? = null,
        messagesJson: String = "[]",
        attachedFilesJson: String = "[]",
        isActive: Boolean = true,
        createdAt: Long = 1000L,
        accentColor: Long = 0xFF6750A4
    ) = TabEntity(
        id = id,
        title = title,
        aiProviderId = aiProviderId,
        agentId = agentId,
        messagesJson = messagesJson,
        attachedFilesJson = attachedFilesJson,
        isActive = isActive,
        createdAt = createdAt,
        accentColor = accentColor
    )

    private fun createTab(
        id: String = "tab-1",
        title: String = "Test Tab",
        aiProviderId: String? = "provider-1",
        agentId: String? = null,
        messages: List<Message> = emptyList(),
        isActive: Boolean = true,
        createdAt: Long = 1000L,
        accentColor: Long = 0xFF6750A4
    ) = Tab(
        id = id,
        title = title,
        aiProviderId = aiProviderId,
        agentId = agentId,
        messages = messages,
        attachedFiles = emptyList(),
        isActive = isActive,
        createdAt = createdAt,
        accentColor = accentColor
    )

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        repository = TabRepositoryImpl(dao)
    }

    @Test
    fun `getAllTabs maps entities to domain`() = runTest {
        val entities = listOf(
            createEntity(id = "1", title = "Tab A"),
            createEntity(id = "2", title = "Tab B")
        )
        every { dao.getAllTabs() } returns flowOf(entities)

        val tabs = repository.getAllTabs().first()

        assertEquals(2, tabs.size)
        assertEquals("Tab A", tabs[0].title)
        assertEquals("Tab B", tabs[1].title)
    }

    @Test
    fun `getAllTabs returns empty list when no tabs`() = runTest {
        every { dao.getAllTabs() } returns flowOf(emptyList())

        val tabs = repository.getAllTabs().first()

        assertEquals(0, tabs.size)
    }

    @Test
    fun `getTabById returns domain model`() = runTest {
        val entity = createEntity(id = "42", title = "My Tab")
        coEvery { dao.getTabById("42") } returns entity

        val tab = repository.getTabById("42")

        assertNotNull(tab)
        assertEquals("42", tab?.id)
        assertEquals("My Tab", tab?.title)
    }

    @Test
    fun `getTabById returns null when not found`() = runTest {
        coEvery { dao.getTabById("999") } returns null

        val tab = repository.getTabById("999")

        assertNull(tab)
    }

    @Test
    fun `createTab inserts entity and returns tab`() = runTest {
        val tab = createTab(id = "new-1", title = "New Tab")

        val result = repository.createTab(tab)

        coVerify { dao.insertTab(match {
            it.id == "new-1" && it.title == "New Tab"
        }) }
        assertEquals("new-1", result.id)
        assertEquals("New Tab", result.title)
    }

    @Test
    fun `updateTab calls dao updateTab`() = runTest {
        val tab = createTab(id = "1", title = "Updated Tab")

        repository.updateTab(tab)

        coVerify { dao.updateTab(match {
            it.id == "1" && it.title == "Updated Tab"
        }) }
    }

    @Test
    fun `deleteTab calls dao deleteTabById`() = runTest {
        repository.deleteTab("1")

        coVerify { dao.deleteTabById("1") }
    }

    @Test
    fun `searchTabs finds matching tabs by title`() = runTest {
        val entities = listOf(createEntity(id = "1", title = "AI Chat"))
        every { dao.searchTabs("AI") } returns flowOf(entities)

        val tabs = repository.searchTabs("AI").first()

        assertEquals(1, tabs.size)
        assertEquals("AI Chat", tabs[0].title)
    }

    @Test
    fun `searchTabs returns empty when no match`() = runTest {
        every { dao.searchTabs("xyz") } returns flowOf(emptyList())

        val tabs = repository.searchTabs("xyz").first()

        assertEquals(0, tabs.size)
    }

    @Test
    fun `setActiveTab deactivates all then sets active`() = runTest {
        repository.setActiveTab("tab-1")

        coVerify { dao.deactivateAllTabs() }
        coVerify { dao.setActiveTab("tab-1") }
    }

    @Test
    fun `toDomain mapping preserves all fields`() = runTest {
        val entity = createEntity(
            id = "10",
            title = "Full Tab",
            aiProviderId = "prov-1",
            agentId = "agent-1",
            isActive = true,
            createdAt = 5000L,
            accentColor = 0xFF000000
        )
        coEvery { dao.getTabById("10") } returns entity

        val tab = repository.getTabById("10")!!

        assertEquals("10", tab.id)
        assertEquals("Full Tab", tab.title)
        assertEquals("prov-1", tab.aiProviderId)
        assertEquals("agent-1", tab.agentId)
        assertEquals(true, tab.isActive)
        assertEquals(5000L, tab.createdAt)
        assertEquals(0xFF000000, tab.accentColor)
    }

    @Test
    fun `toDomain handles invalid JSON gracefully`() = runTest {
        val entity = createEntity(
            messagesJson = "invalid-json",
            attachedFilesJson = "also-invalid"
        )
        coEvery { dao.getTabById("bad") } returns entity

        val tab = repository.getTabById("bad")!!

        assertEquals(emptyList<Message>(), tab.messages)
        assertEquals(emptyList<Any>(), tab.attachedFiles)
    }
}
