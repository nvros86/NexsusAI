package com.nexusai.data.repository

import android.content.Context
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MarketplaceRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var repository: MarketplaceRepositoryImpl
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
        context = mockk(relaxed = true)
        repository = MarketplaceRepositoryImpl(context)
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `getAllPresets returns all 10 presets`() = runTest {
        val presets = repository.getAllPresets().first()
        assertEquals(10, presets.size)
    }

    @Test
    fun `getAllPresets returns presets with default isAdded false`() = runTest {
        val presets = repository.getAllPresets().first()
        assertTrue(presets.all { !it.isAdded })
    }

    @Test
    fun `getAllPresets contains openai_gpt4o`() = runTest {
        val presets = repository.getAllPresets().first()
        val gpt4o = presets.first { it.id == "openai_gpt4o" }
        assertEquals("GPT-4o", gpt4o.name)
        assertEquals("https://api.openai.com/v1", gpt4o.baseUrl)
        assertEquals("gpt-4o", gpt4o.defaultModel)
    }

    @Test
    fun `getAllPresets contains anthropic_claude4_opus`() = runTest {
        val presets = repository.getAllPresets().first()
        val claude = presets.first { it.id == "anthropic_claude4_opus" }
        assertEquals("Claude 4 Opus", claude.name)
        assertEquals(8192, claude.maxTokens)
    }

    @Test
    fun `getAllPresets contains stability_sdxl`() = runTest {
        val presets = repository.getAllPresets().first()
        val sd = presets.first { it.id == "stability_sdxl" }
        assertEquals("Stable Diffusion XL", sd.name)
        assertEquals(com.nexusai.domain.model.MarketplaceCategory.IMAGE, sd.category)
    }

    @Test
    fun `getAllPresets contains elevenlabs_tts`() = runTest {
        val presets = repository.getAllPresets().first()
        val tts = presets.first { it.id == "elevenlabs_tts" }
        assertEquals("ElevenLabs TTS", tts.name)
        assertEquals(com.nexusai.domain.model.MarketplaceCategory.VOICE, tts.category)
    }

    @Test
    fun `getAllPresets contains runway_gen3`() = runTest {
        val presets = repository.getAllPresets().first()
        val runway = presets.first { it.id == "runway_gen3" }
        assertEquals("Runway Gen-3", runway.name)
        assertEquals(com.nexusai.domain.model.MarketplaceCategory.VIDEO, runway.category)
    }

    @Test
    fun `getAllPresets contains deepseek_chat`() = runTest {
        val presets = repository.getAllPresets().first()
        val deepseek = presets.first { it.id == "deepseek_chat" }
        assertEquals("DeepSeek Chat", deepseek.name)
        assertEquals(com.nexusai.domain.model.MarketplaceCategory.CODE, deepseek.category)
    }

    @Test
    fun `markAsAdded and getAddedProviderIds work`() = runTest {
        repository.markAsAdded("openai_gpt4o")
        val ids = repository.getAddedProviderIds()
        assertEquals(setOf("openai_gpt4o"), ids)
    }

    @Test
    fun `markAsAdded multiple ids accumulates`() = runTest {
        repository.markAsAdded("openai_gpt4o")
        repository.markAsAdded("anthropic_claude4_opus")
        val ids = repository.getAddedProviderIds()
        assertEquals(setOf("openai_gpt4o", "anthropic_claude4_opus"), ids)
    }

    @Test
    fun `getAddedProviderIds returns empty set initially`() = runTest {
        val ids = repository.getAddedProviderIds()
        assertTrue(ids.isEmpty())
    }

    @Test
    fun `getAllPresets reflects isAdded after markAsAdded`() = runTest {
        repository.markAsAdded("openai_gpt4o")
        val presets = repository.getAllPresets().first()
        val gpt4o = presets.first { it.id == "openai_gpt4o" }
        assertTrue(gpt4o.isAdded)
        val other = presets.first { it.id == "anthropic_claude4_opus" }
        assertFalse(other.isAdded)
    }

    @Test
    fun `getPresetsByCategory TEXT returns text providers`() = runTest {
        val presets = repository.getPresetsByCategory("TEXT").first()
        assertTrue(presets.isNotEmpty())
        assertTrue(presets.all { it.category.name == "TEXT" })
        assertTrue(presets.any { it.id == "openai_gpt4o" })
        assertTrue(presets.any { it.id == "anthropic_claude4_opus" })
        assertTrue(presets.any { it.id == "gemini_2_5_pro" })
    }

    @Test
    fun `getPresetsByCategory IMAGE returns image providers`() = runTest {
        val presets = repository.getPresetsByCategory("IMAGE").first()
        assertEquals(1, presets.size)
        assertEquals("stability_sdxl", presets.first().id)
    }

    @Test
    fun `getPresetsByCategory VOICE returns voice providers`() = runTest {
        val presets = repository.getPresetsByCategory("VOICE").first()
        assertEquals(1, presets.size)
        assertEquals("elevenlabs_tts", presets.first().id)
    }

    @Test
    fun `getPresetsByCategory VIDEO returns video providers`() = runTest {
        val presets = repository.getPresetsByCategory("VIDEO").first()
        assertEquals(1, presets.size)
        assertEquals("runway_gen3", presets.first().id)
    }

    @Test
    fun `getPresetsByCategory CODE returns code providers`() = runTest {
        val presets = repository.getPresetsByCategory("CODE").first()
        assertEquals(1, presets.size)
        assertEquals("deepseek_chat", presets.first().id)
    }

    @Test
    fun `getPresetsByCategory nonexistent returns empty`() = runTest {
        val presets = repository.getPresetsByCategory("NONEXISTENT").first()
        assertTrue(presets.isEmpty())
    }

    @Test
    fun `getPresetsByCategory reflects isAdded state`() = runTest {
        repository.markAsAdded("openai_gpt4o")
        val presets = repository.getPresetsByCategory("TEXT").first()
        val gpt4o = presets.first { it.id == "openai_gpt4o" }
        assertTrue(gpt4o.isAdded)
    }

    @Test
    fun `searchPresets by name matches`() = runTest {
        val presets = repository.searchPresets("GPT-4o").first()
        assertTrue(presets.isNotEmpty())
        assertTrue(presets.any { it.id == "openai_gpt4o" })
    }

    @Test
    fun `searchPresets by description matches`() = runTest {
        val presets = repository.searchPresets("Флагманская").first()
        assertTrue(presets.isNotEmpty())
        assertTrue(presets.any { it.id == "openai_gpt4o" })
    }

    @Test
    fun `searchPresets by category displayName matches`() = runTest {
        val presets = repository.searchPresets("Генерация изображений").first()
        assertTrue(presets.isNotEmpty())
        assertTrue(presets.any { it.id == "stability_sdxl" })
    }

    @Test
    fun `searchPresets case insensitive`() = runTest {
        val lower = repository.searchPresets("claude").first()
        val upper = repository.searchPresets("CLAUDE").first()
        assertEquals(lower.size, upper.size)
        assertTrue(lower.isNotEmpty())
    }

    @Test
    fun `searchPresets partial match works`() = runTest {
        val presets = repository.searchPresets("Gemini").first()
        assertTrue(presets.any { it.id == "gemini_2_5_pro" })
    }

    @Test
    fun `searchPresets no match returns empty`() = runTest {
        val presets = repository.searchPresets("несуществующий_запрос_xyz").first()
        assertTrue(presets.isEmpty())
    }

    @Test
    fun `searchPresets with empty query returns all`() = runTest {
        val presets = repository.searchPresets("").first()
        assertEquals(10, presets.size)
    }

    @Test
    fun `searchPresets reflects isAdded state`() = runTest {
        repository.markAsAdded("openai_gpt4o")
        val presets = repository.searchPresets("GPT-4o").first()
        assertTrue(presets.first().isAdded)
    }

    @Test
    fun `all providers have required fields`() = runTest {
        val presets = repository.getAllPresets().first()
        presets.forEach { provider ->
            assertTrue(provider.id.isNotEmpty())
            assertTrue(provider.name.isNotEmpty())
            assertTrue(provider.description.isNotEmpty())
            assertTrue(provider.baseUrl.isNotEmpty())
            assertTrue(provider.models.isNotEmpty())
            assertTrue(provider.defaultModel.isNotEmpty())
            assertTrue(provider.logoEmoji.isNotEmpty())
            assertTrue(provider.capabilities.isNotEmpty())
        }
    }
}
