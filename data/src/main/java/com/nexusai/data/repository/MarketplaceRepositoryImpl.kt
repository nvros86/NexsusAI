package com.nexusai.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.nexusai.domain.model.MarketplaceCategory
import com.nexusai.domain.model.MarketplaceProvider
import com.nexusai.domain.model.ProviderCapability
import com.nexusai.domain.model.ProviderType
import com.nexusai.domain.repository.MarketplaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketplaceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MarketplaceRepository {

    private val addedIds = MutableStateFlow<Set<String>>(emptySet())

    private val presets = listOf(
        MarketplaceProvider(
            id = "openai_gpt4o",
            name = "GPT-4o",
            description = "Флагманская модель OpenAI. Быстрая, умная, поддерживает изображения и код.",
            type = ProviderType.OPENAI,
            baseUrl = "https://api.openai.com/v1",
            models = listOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo"),
            defaultModel = "gpt-4o",
            maxTokens = 4096,
            category = MarketplaceCategory.TEXT,
            capabilities = listOf(
                ProviderCapability.TEXT_GENERATION,
                ProviderCapability.CODE_GENERATION,
                ProviderCapability.FILE_ANALYSIS,
                ProviderCapability.STREAMING,
                ProviderCapability.FUNCTION_CALLING,
                ProviderCapability.VISION
            ),
            logoEmoji = "🟢",
            websiteUrl = "https://openai.com"
        ),
        MarketplaceProvider(
            id = "openai_gpt4o_mini",
            name = "GPT-4o Mini",
            description = "Лёгкая и быстрая модель от OpenAI. Идеальна для повседневных задач.",
            type = ProviderType.OPENAI,
            baseUrl = "https://api.openai.com/v1",
            models = listOf("gpt-4o-mini"),
            defaultModel = "gpt-4o-mini",
            maxTokens = 4096,
            category = MarketplaceCategory.TEXT,
            capabilities = listOf(
                ProviderCapability.TEXT_GENERATION,
                ProviderCapability.CODE_GENERATION,
                ProviderCapability.STREAMING
            ),
            logoEmoji = "🟢",
            websiteUrl = "https://openai.com"
        ),
        MarketplaceProvider(
            id = "anthropic_claude4_opus",
            name = "Claude 4 Opus",
            description = "Мощнейшая модель Anthropic. Глубокий анализ, код, длинный контекст (200K).",
            type = ProviderType.ANTHROPIC,
            baseUrl = "https://api.anthropic.com/v1",
            models = listOf("claude-4-opus", "claude-sonnet-4-20250514"),
            defaultModel = "claude-sonnet-4-20250514",
            maxTokens = 8192,
            category = MarketplaceCategory.TEXT,
            capabilities = listOf(
                ProviderCapability.TEXT_GENERATION,
                ProviderCapability.CODE_GENERATION,
                ProviderCapability.FILE_ANALYSIS,
                ProviderCapability.STREAMING,
                ProviderCapability.VISION
            ),
            logoEmoji = "🟠",
            websiteUrl = "https://anthropic.com"
        ),
        MarketplaceProvider(
            id = "anthropic_claude_sonnet",
            name = "Claude Sonnet",
            description = "Баланс скорости и качества от Anthropic. Отлична для кода и анализа.",
            type = ProviderType.ANTHROPIC,
            baseUrl = "https://api.anthropic.com/v1",
            models = listOf("claude-sonnet-4-20250514", "claude-3-5-haiku-20241022"),
            defaultModel = "claude-sonnet-4-20250514",
            maxTokens = 8192,
            category = MarketplaceCategory.TEXT,
            capabilities = listOf(
                ProviderCapability.TEXT_GENERATION,
                ProviderCapability.CODE_GENERATION,
                ProviderCapability.STREAMING
            ),
            logoEmoji = "🟠",
            websiteUrl = "https://anthropic.com"
        ),
        MarketplaceProvider(
            id = "gemini_2_5_pro",
            name = "Gemini 2.5 Pro",
            description = "Мультимодальная модель Google. Контекст до 1M токенов, видео, аудио.",
            type = ProviderType.GEMINI,
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            models = listOf("gemini-2.5-pro", "gemini-2.5-flash"),
            defaultModel = "gemini-2.5-pro",
            maxTokens = 8192,
            category = MarketplaceCategory.TEXT,
            capabilities = listOf(
                ProviderCapability.TEXT_GENERATION,
                ProviderCapability.CODE_GENERATION,
                ProviderCapability.FILE_ANALYSIS,
                ProviderCapability.STREAMING,
                ProviderCapability.VISION
            ),
            logoEmoji = "🔵",
            websiteUrl = "https://ai.google.dev"
        ),
        MarketplaceProvider(
            id = "stability_sdxl",
            name = "Stable Diffusion XL",
            description = "Генерация высококачественных изображений от Stability AI.",
            type = ProviderType.STABILITY,
            baseUrl = "https://api.stability.ai/v2beta",
            models = listOf("stable-diffusion-xl-1024-v1-0", "stable-diffusion-xl-1024-v0-9"),
            defaultModel = "stable-diffusion-xl-1024-v1-0",
            category = MarketplaceCategory.IMAGE,
            capabilities = listOf(
                ProviderCapability.IMAGE_GENERATION
            ),
            logoEmoji = "🟣",
            websiteUrl = "https://stability.ai"
        ),
        MarketplaceProvider(
            id = "elevenlabs_tts",
            name = "ElevenLabs TTS",
            description = "Лучший синтез речи. Реалистичные голоса, клонирование голоса.",
            type = ProviderType.ELEVENLABS,
            baseUrl = "https://api.elevenlabs.io/v1",
            models = listOf("eleven_multilingual_v2", "eleven_monolingual_v1"),
            defaultModel = "eleven_multilingual_v2",
            category = MarketplaceCategory.VOICE,
            capabilities = listOf(
                ProviderCapability.VOICE
            ),
            logoEmoji = "🔊",
            websiteUrl = "https://elevenlabs.io"
        ),
        MarketplaceProvider(
            id = "runway_gen3",
            name = "Runway Gen-3",
            description = "Генерация видео из текста и изображений. Киностудия в кармане.",
            type = ProviderType.RUNWAY,
            baseUrl = "https://api.runwayml.com/v1",
            models = listOf("gen3a_turbo"),
            defaultModel = "gen3a_turbo",
            category = MarketplaceCategory.VIDEO,
            capabilities = listOf(
                ProviderCapability.IMAGE_GENERATION
            ),
            logoEmoji = "🎬",
            websiteUrl = "https://runwayml.com"
        ),
        MarketplaceProvider(
            id = "mistral_large",
            name = "Mistral Large",
            description = "Мощная модель с открытым исходным кодом. Быстрая и эффективная.",
            type = ProviderType.CUSTOM,
            baseUrl = "https://api.mistral.ai/v1",
            models = listOf("mistral-large-latest", "mistral-medium-latest"),
            defaultModel = "mistral-large-latest",
            maxTokens = 4096,
            category = MarketplaceCategory.TEXT,
            capabilities = listOf(
                ProviderCapability.TEXT_GENERATION,
                ProviderCapability.CODE_GENERATION,
                ProviderCapability.STREAMING,
                ProviderCapability.FUNCTION_CALLING
            ),
            logoEmoji = "🟤",
            websiteUrl = "https://mistral.ai"
        ),
        MarketplaceProvider(
            id = "deepseek_chat",
            name = "DeepSeek Chat",
            description = "Модель для кода и рассуждений. Конкурент GPT-4 по качеству.",
            type = ProviderType.CUSTOM,
            baseUrl = "https://api.deepseek.com/v1",
            models = listOf("deepseek-chat", "deepseek-coder"),
            defaultModel = "deepseek-chat",
            maxTokens = 4096,
            category = MarketplaceCategory.CODE,
            capabilities = listOf(
                ProviderCapability.TEXT_GENERATION,
                ProviderCapability.CODE_GENERATION,
                ProviderCapability.STREAMING
            ),
            logoEmoji = "🐋",
            websiteUrl = "https://deepseek.com"
        )
    )

    override fun getAllPresets(): Flow<List<MarketplaceProvider>> {
        return addedIds.map { ids ->
            presets.map { it.copy(isAdded = ids.contains(it.id)) }
        }
    }

    override fun getPresetsByCategory(category: String): Flow<List<MarketplaceProvider>> {
        return addedIds.map { ids ->
            presets.filter { it.category.name == category }
                .map { it.copy(isAdded = ids.contains(it.id)) }
        }
    }

    override fun searchPresets(query: String): Flow<List<MarketplaceProvider>> {
        return addedIds.map { ids ->
            presets.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true) ||
                        it.category.displayName.contains(query, ignoreCase = true)
            }.map { it.copy(isAdded = ids.contains(it.id)) }
        }
    }

    override suspend fun getAddedProviderIds(): Set<String> = addedIds.value

    override suspend fun markAsAdded(id: String) {
        addedIds.value = addedIds.value + id
    }
}
