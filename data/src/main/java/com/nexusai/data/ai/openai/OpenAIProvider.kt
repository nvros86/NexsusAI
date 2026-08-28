package com.nexusai.data.ai.openai

import com.nexusai.domain.ai.AIProvider
import com.nexusai.domain.ai.AIResponse
import com.nexusai.domain.ai.ChatMessage
import com.nexusai.domain.ai.MessageRole
import com.nexusai.domain.ai.TokenUsage
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class OpenAIProvider(
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val baseUrl: String = "https://api.openai.com/v1"
) : AIProvider {

    override val id = "openai"
    override val name = "OpenAI"
    override val type = "OPENAI"
    override val supportsStreaming = true

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun sendMessage(
        messages: List<ChatMessage>,
        model: String,
        maxTokens: Int,
        temperature: Float
    ): AIResponse {
        val request = OpenAIRequest(
            model = model,
            messages = messages.map { it.toOpenAI() },
            maxTokens = maxTokens,
            temperature = temperature,
            stream = false
        )

        val response = httpClient.post("$baseUrl/chat/completions") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $apiKey")
            setBody(json.encodeToString(OpenAIRequest.serializer(), request))
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw Exception("OpenAI API error: ${response.status} - $errorBody")
        }

        val responseBody = response.bodyAsText()
        val openAIResponse = json.decodeFromString(OpenAIResponse.serializer(), responseBody)

        val choice = openAIResponse.choices.firstOrNull()
            ?: throw Exception("No response from OpenAI")

        return AIResponse(
            content = choice.message?.content ?: "",
            model = openAIResponse.model,
            usage = openAIResponse.usage?.let {
                TokenUsage(
                    promptTokens = it.promptTokens,
                    completionTokens = it.completionTokens,
                    totalTokens = it.totalTokens
                )
            }
        )
    }

    override fun sendMessageStream(
        messages: List<ChatMessage>,
        model: String,
        maxTokens: Int,
        temperature: Float
    ): Flow<String> = flow {
        val request = OpenAIRequest(
            model = model,
            messages = messages.map { it.toOpenAI() },
            maxTokens = maxTokens,
            temperature = temperature,
            stream = true
        )

        val response = httpClient.post("$baseUrl/chat/completions") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $apiKey")
            header("Accept", "text/event-stream")
            setBody(json.encodeToString(OpenAIRequest.serializer(), request))
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw Exception("OpenAI API error: ${response.status} - $errorBody")
        }

        val channel = response.bodyAsChannel()
        val buffer = StringBuilder()

        while (!channel.isClosedForRead) {
            val chunk = channel.readUTF8Line() ?: continue
            buffer.clear()
            buffer.append(chunk)

            if (chunk.startsWith("data: ")) {
                val data = chunk.removePrefix("data: ").trim()
                if (data == "[DONE]") break

                try {
                    val streamResponse = json.decodeFromString(OpenAIResponse.serializer(), data)
                    val delta = streamResponse.choices.firstOrNull()?.delta?.content
                    if (delta != null) {
                        emit(delta)
                    }
                } catch (e: Exception) {
                    // Skip malformed chunks
                }
            }
        }
    }

    override suspend fun listModels(): List<String> {
        return listOf(
            "gpt-4o",
            "gpt-4o-mini",
            "gpt-4-turbo",
            "gpt-3.5-turbo"
        )
    }

    private fun ChatMessage.toOpenAI() = OpenAIMessage(
        role = when (role) {
            MessageRole.SYSTEM -> "system"
            MessageRole.USER -> "user"
            MessageRole.ASSISTANT -> "assistant"
        },
        content = content
    )
}
