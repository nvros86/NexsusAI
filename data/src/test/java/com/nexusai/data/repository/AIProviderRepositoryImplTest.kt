package com.nexusai.data.repository

import com.nexusai.data.local.AIProviderDao
import com.nexusai.data.local.AIProviderEntity
import com.nexusai.domain.model.ProviderType
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
class AIProviderRepositoryImplTest {

    private lateinit var dao: AIProviderDao
    private lateinit var repository: AIProviderRepositoryImpl

    private fun createEntity(
        id: String = "1",
        name: String = "Test Provider",
        type: String = "OPENAI",
        isFavorite: Boolean = false
    ) = AIProviderEntity(
        id = id,
        name = name,
        type = type,
        baseUrl = "https://api.openai.com/v1",
        apiKeyEncrypted = "encrypted-key",
        modelsJson = "[\"gpt-4\"]",
        defaultModel = "gpt-4",
        maxTokens = 4096,
        temperature = 0.7f,
        systemPrompt = "",
        customHeadersJson = "{}",
        supportsImages = false,
        supportsFiles = false,
        supportsStreaming = true,
        isFavorite = isFavorite
    )

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        repository = AIProviderRepositoryImpl(dao)
    }

    @Test
    fun `getAllProviders maps entities to domain`() = runTest {
        val entities = listOf(createEntity(id = "1", name = "Provider A"))
        every { dao.getAllProviders() } returns flowOf(entities)

        val providers = repository.getAllProviders().first()

        assertEquals(1, providers.size)
        assertEquals("Provider A", providers[0].name)
        assertEquals(ProviderType.OPENAI, providers[0].type)
    }

    @Test
    fun `getProviderById returns domain model`() = runTest {
        val entity = createEntity(id = "42", name = "My Provider")
        coEvery { dao.getProviderById("42") } returns entity

        val provider = repository.getProviderById("42")

        assertNotNull(provider)
        assertEquals("My Provider", provider?.name)
    }

    @Test
    fun `getProviderById returns null when not found`() = runTest {
        coEvery { dao.getProviderById("999") } returns null

        val provider = repository.getProviderById("999")

        assertNull(provider)
    }

    @Test
    fun `addProvider inserts entity`() = runTest {
        val provider = com.nexusai.domain.model.AIProviderConfig(
            id = "1",
            name = "New Provider",
            type = ProviderType.ANTHROPIC,
            baseUrl = "https://api.anthropic.com",
            apiKey = "key",
            defaultModel = "claude-3"
        )

        repository.addProvider(provider)

        coVerify { dao.insertProvider(any()) }
    }

    @Test
    fun `updateProvider updates entity`() = runTest {
        val provider = com.nexusai.domain.model.AIProviderConfig(
            id = "1",
            name = "Updated",
            type = ProviderType.OPENAI,
            baseUrl = "https://api.openai.com/v1"
        )

        repository.updateProvider(provider)

        coVerify { dao.updateProvider(any()) }
    }

    @Test
    fun `deleteProvider deletes by id`() = runTest {
        repository.deleteProvider("1")

        coVerify { dao.deleteProviderById("1") }
    }

    @Test
    fun `getFavoriteProviders returns only favorites`() = runTest {
        val favorites = listOf(createEntity(id = "1", isFavorite = true))
        coEvery { dao.getFavoriteProviders() } returns favorites

        val result = repository.getFavoriteProviders()

        assertEquals(1, result.size)
        assertEquals(true, result[0].isFavorite)
    }

    @Test
    fun `toDomain mapping preserves all fields`() = runTest {
        val entity = createEntity(id = "10", name = "Full Test").copy(
            supportsImages = true,
            supportsFiles = true,
            supportsStreaming = false,
            isFavorite = true,
            systemPrompt = "You are helpful."
        )
        coEvery { dao.getProviderById("10") } returns entity

        val provider = repository.getProviderById("10")!!

        assertEquals("10", provider.id)
        assertEquals("Full Test", provider.name)
        assertEquals(ProviderType.OPENAI, provider.type)
        assertEquals(true, provider.supportsImages)
        assertEquals(true, provider.supportsFiles)
        assertEquals(false, provider.supportsStreaming)
        assertEquals(true, provider.isFavorite)
        assertEquals("You are helpful.", provider.systemPrompt)
    }
}
