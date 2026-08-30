package com.nexusai.data.ai

import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.ProviderType
import com.nexusai.domain.model.RoutingStrategy
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AIRouterTest {

    private lateinit var router: AIRouter

    private val openaiProvider = AIProviderConfig(
        id = "openai",
        name = "OpenAI",
        type = ProviderType.OPENAI,
        baseUrl = "https://api.openai.com/v1",
        apiKey = "sk-test",
        models = listOf("gpt-4o", "gpt-4o-mini"),
        defaultModel = "gpt-4o"
    )

    private val anthropicProvider = AIProviderConfig(
        id = "anthropic",
        name = "Anthropic",
        type = ProviderType.ANTHROPIC,
        baseUrl = "https://api.anthropic.com/v1",
        apiKey = "sk-ant-test",
        models = listOf("claude-sonnet-4-20250514", "claude-3-haiku"),
        defaultModel = "claude-sonnet-4-20250514"
    )

    private val geminiProvider = AIProviderConfig(
        id = "gemini",
        name = "Gemini",
        type = ProviderType.GEMINI,
        baseUrl = "https://generativelanguage.googleapis.com/v1",
        apiKey = "ai-test",
        models = listOf("gemini-2.5-pro"),
        defaultModel = "gemini-2.5-pro"
    )

    private val localProvider = AIProviderConfig(
        id = "local",
        name = "Local LLM",
        type = ProviderType.LOCAL,
        baseUrl = "http://localhost:11434",
        apiKey = "",
        models = listOf("llama3"),
        defaultModel = "llama3"
    )

    private val customProvider = AIProviderConfig(
        id = "custom",
        name = "Custom",
        type = ProviderType.CUSTOM,
        baseUrl = "https://custom.api.com",
        apiKey = "custom-key",
        models = listOf("custom-model"),
        defaultModel = "custom-model"
    )

    @Before
    fun setUp() {
        router = AIRouter()
    }

    @Test
    fun `route returns null for empty providers`() {
        val result = router.route(emptyList(), RoutingStrategy.BEST_QUALITY)
        assertNull(result)
    }

    @Test
    fun `route selects Anthropic for BEST_QUALITY`() {
        val result = router.route(
            listOf(openaiProvider, anthropicProvider, geminiProvider),
            RoutingStrategy.BEST_QUALITY
        )!!
        assertEquals("anthropic", result.selectedProvider.id)
        assertEquals(RoutingStrategy.BEST_QUALITY, result.strategy)
    }

    @Test
    fun `route selects cheapest provider for CHEAPEST strategy`() {
        val result = router.route(
            listOf(anthropicProvider, openaiProvider, geminiProvider),
            RoutingStrategy.CHEAPEST
        )!!
        assertEquals("gemini", result.selectedProvider.id)
    }

    @Test
    fun `route selects fastest provider for FASTEST strategy`() {
        val result = router.route(
            listOf(anthropicProvider, openaiProvider, geminiProvider),
            RoutingStrategy.FASTEST
        )!!
        assertEquals("gemini", result.selectedProvider.id)
    }

    @Test
    fun `route prefers LOCAL for FASTEST`() {
        val result = router.route(
            listOf(openaiProvider, localProvider),
            RoutingStrategy.FASTEST
        )!!
        assertEquals("local", result.selectedProvider.id)
    }

    @Test
    fun `route excludes providers without API key`() {
        val result = router.route(
            listOf(localProvider, openaiProvider),
            RoutingStrategy.BEST_QUALITY
        )!!
        assertEquals("openai", result.selectedProvider.id)
    }

    @Test
    fun `route returns null when no providers have API key`() {
        val result = router.route(
            listOf(localProvider),
            RoutingStrategy.BEST_QUALITY
        )
        assertNull(result)
    }

    @Test
    fun `route includes failover chain`() {
        val result = router.route(
            listOf(anthropicProvider, openaiProvider, geminiProvider),
            RoutingStrategy.BALANCED
        )!!
        assertTrue(result.failoverChain.isNotEmpty())
        assertTrue(result.failoverChain.size <= 2)
        assertFalse(result.failoverChain.any { it.id == result.selectedProvider.id })
    }

    @Test
    fun `route with single provider returns empty failover`() {
        val result = router.route(
            listOf(openaiProvider),
            RoutingStrategy.BEST_QUALITY
        )!!
        assertEquals("openai", result.selectedProvider.id)
        assertTrue(result.failoverChain.isEmpty())
    }

    @Test
    fun `route selects correct model from provider`() {
        val result = router.route(
            listOf(openaiProvider),
            RoutingStrategy.BEST_QUALITY
        )!!
        assertEquals("gpt-4o", result.selectedModel)
    }

    @Test
    fun `route uses defaultModel when available`() {
        val provider = openaiProvider.copy(defaultModel = "gpt-4o-mini")
        val result = router.route(listOf(provider), RoutingStrategy.BEST_QUALITY)!!
        assertEquals("gpt-4o-mini", result.selectedModel)
    }

    @Test
    fun `route falls back to first model when defaultModel empty`() {
        val provider = openaiProvider.copy(defaultModel = "", models = listOf("gpt-4o-mini", "gpt-4o"))
        val result = router.route(listOf(provider), RoutingStrategy.BEST_QUALITY)!!
        assertEquals("gpt-4o-mini", result.selectedModel)
    }

    @Test
    fun `route uses fallback model names when provider has no models`() {
        val provider = openaiProvider.copy(models = emptyList(), defaultModel = "")
        val result = router.route(listOf(provider), RoutingStrategy.BEST_QUALITY)!!
        assertEquals("gpt-4o", result.selectedModel)
    }

    @Test
    fun `route fallback model for ANTHROPIC`() {
        val provider = anthropicProvider.copy(models = emptyList(), defaultModel = "")
        val result = router.route(listOf(provider), RoutingStrategy.BEST_QUALITY)!!
        assertEquals("claude-sonnet-4-20250514", result.selectedModel)
    }

    @Test
    fun `route fallback model for GEMINI`() {
        val provider = geminiProvider.copy(models = emptyList(), defaultModel = "")
        val result = router.route(listOf(provider), RoutingStrategy.BEST_QUALITY)!!
        assertEquals("gemini-2.5-pro", result.selectedModel)
    }

    @Test
    fun `route fallback model for CUSTOM`() {
        val provider = customProvider.copy(models = emptyList(), defaultModel = "")
        val result = router.route(listOf(provider), RoutingStrategy.BEST_QUALITY)!!
        assertEquals("default", result.selectedModel)
    }

    @Test
    fun `route score is positive`() {
        val result = router.route(
            listOf(openaiProvider),
            RoutingStrategy.BEST_QUALITY
        )!!
        assertTrue(result.score > 0f)
    }

    @Test
    fun `route reason is not empty`() {
        val result = router.route(
            listOf(openaiProvider),
            RoutingStrategy.BEST_QUALITY
        )!!
        assertTrue(result.reason.isNotEmpty())
        assertTrue(result.reason.contains("OpenAI"))
    }

    @Test
    fun `BALANCED strategy gives equal weight`() {
        val result = router.route(
            listOf(anthropicProvider, openaiProvider, geminiProvider),
            RoutingStrategy.BALANCED
        )!!
        assertNotNull(result.selectedProvider)
        assertTrue(result.score > 0f)
    }

    @Test
    fun `FALLBACK_ONLY strategy only checks availability`() {
        val resultWithKey = router.route(
            listOf(openaiProvider),
            RoutingStrategy.FALLBACK_ONLY
        )!!
        assertEquals(1.0f, resultWithKey.score, 0.01f)
    }

    @Test
    fun `route with custom provider works`() {
        val result = router.route(
            listOf(customProvider),
            RoutingStrategy.BEST_QUALITY
        )!!
        assertEquals("custom", result.selectedProvider.id)
    }
}
