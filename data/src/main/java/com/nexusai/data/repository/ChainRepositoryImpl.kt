package com.nexusai.data.repository

import com.nexusai.data.ai.AIProviderManager
import com.nexusai.domain.ai.ChatMessage
import com.nexusai.domain.model.MessageRole
import com.nexusai.domain.model.AutomationChain
import com.nexusai.domain.model.ChainRunResult
import com.nexusai.domain.model.ChainStep
import com.nexusai.domain.model.ChainStepResult
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.repository.AIProviderRepository
import com.nexusai.domain.repository.ChainRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChainRepositoryImpl @Inject constructor(
    private val providerRepository: AIProviderRepository,
    private val aiProviderManager: AIProviderManager
) : ChainRepository {

    private val chains = MutableStateFlow<List<AutomationChain>>(emptyList())

    init {
        chains.value = listOf(
            AutomationChain(
                id = "video_pipeline",
                name = "Видео-пайплайн",
                description = "Скрипт → Превью → Описание",
                steps = listOf(
                    ChainStep(
                        id = "step1",
                        type = com.nexusai.domain.model.ChainStepType.TEXT_GENERATION,
                        name = "Написать скрипт",
                        prompt = "Напиши короткий скрипт для видео на тему: {input}",
                        outputKey = "script"
                    ),
                    ChainStep(
                        id = "step2",
                        type = com.nexusai.domain.model.ChainStepType.IMAGE_GENERATION,
                        name = "Сгенерировать превью",
                        prompt = "Сгенерируй изображение для видео: {script}",
                        outputKey = "thumbnail"
                    ),
                    ChainStep(
                        id = "step3",
                        type = com.nexusai.domain.model.ChainStepType.TEXT_GENERATION,
                        name = "Написать описание",
                        prompt = "Напиши описание для YouTube видео на основе скрипта: {script}",
                        outputKey = "description"
                    )
                )
            ),
            AutomationChain(
                id = "blog_pipeline",
                name = "Блог-пайплайн",
                description = "Тема → Статья → Резюме",
                steps = listOf(
                    ChainStep(
                        id = "step1",
                        type = com.nexusai.domain.model.ChainStepType.TEXT_GENERATION,
                        name = "Написать статью",
                        prompt = "Напиши статью для блога на тему: {input}. Объём 1000-1500 слов.",
                        outputKey = "article"
                    ),
                    ChainStep(
                        id = "step2",
                        type = com.nexusai.domain.model.ChainStepType.SUMMARIZATION,
                        name = "Суммаризировать",
                        prompt = "Суммаризируй статью: {article}",
                        outputKey = "summary"
                    )
                )
            ),
            AutomationChain(
                id = "code_pipeline",
                name = "Код-пайплайн",
                description = "Задача → Код → Тесты",
                steps = listOf(
                    ChainStep(
                        id = "step1",
                        type = com.nexusai.domain.model.ChainStepType.CODE_GENERATION,
                        name = "Написать код",
                        prompt = "Напиши код для: {input}. Используй Kotlin.",
                        outputKey = "code"
                    ),
                    ChainStep(
                        id = "step2",
                        type = com.nexusai.domain.model.ChainStepType.CODE_GENERATION,
                        name = "Написать тесты",
                        prompt = "Напиши юнит-тесты для кода: {code}",
                        outputKey = "tests"
                    )
                )
            )
        )
    }

    override fun getAllChains(): Flow<List<AutomationChain>> = chains

    override suspend fun getChainById(id: String): AutomationChain? {
        return chains.value.find { it.id == id }
    }

    override suspend fun saveChain(chain: AutomationChain) {
        val updated = chain.copy(updatedAt = System.currentTimeMillis())
        chains.value = chains.value.filter { it.id != chain.id } + updated
    }

    override suspend fun deleteChain(id: String) {
        chains.value = chains.value.filter { it.id != id }
    }

    override suspend fun runChain(chain: AutomationChain): ChainRunResult {
        val stepResults = mutableListOf<ChainStepResult>()
        val context = mutableMapOf<String, String>()
        var hasError = false
        var errorMsg: String? = null

        for (step in chain.steps.filter { it.isEnabled }) {
            val startTime = System.currentTimeMillis()

            try {
                val resolvedPrompt = resolvePrompt(step.prompt, context)
                val output = executeStep(step, resolvedPrompt)
                val duration = System.currentTimeMillis() - startTime

                context[step.outputKey] = output

                stepResults.add(
                    ChainStepResult(
                        stepId = step.id,
                        stepName = step.name,
                        input = resolvedPrompt,
                        output = output,
                        durationMs = duration
                    )
                )
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                hasError = true
                errorMsg = e.message

                stepResults.add(
                    ChainStepResult(
                        stepId = step.id,
                        stepName = step.name,
                        input = step.prompt,
                        output = "",
                        durationMs = duration,
                        isError = true,
                        errorMessage = e.message
                    )
                )
                break
            }
        }

        val updatedChain = chain.copy(runCount = chain.runCount + 1)
        saveChain(updatedChain)

        return ChainRunResult(
            chainId = chain.id,
            stepResults = stepResults,
            isError = hasError,
            errorMessage = errorMsg
        )
    }

    private fun resolvePrompt(prompt: String, context: Map<String, String>): String {
        var resolved = prompt
        for ((key, value) in context) {
            resolved = resolved.replace("{$key}", value)
        }
        return resolved
    }

    private suspend fun executeStep(step: ChainStep, prompt: String): String {
        var provider: AIProviderConfig? = null
        providerRepository.getAllProviders().collect { list ->
            provider = list.firstOrNull { it.apiKey.isNotEmpty() }
            return@collect
        }

        val currentProvider = provider
        if (currentProvider == null) {
            throw IllegalStateException("No AI provider available with a configured API key")
        }

        val aiProvider = aiProviderManager.getProvider(currentProvider)
        val model = step.model.ifEmpty {
            currentProvider.defaultModel.ifEmpty { currentProvider.models.firstOrNull() ?: "default" }
        }

        val messages = listOf(
            ChatMessage(role = MessageRole.USER, content = prompt)
        )

        val response = aiProvider.sendMessage(
            messages = messages,
            model = model,
            maxTokens = 4096
        )

        return response.content
    }
}
