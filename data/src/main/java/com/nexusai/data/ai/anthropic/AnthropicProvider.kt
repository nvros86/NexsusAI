package com.nexusai.data.ai.anthropic

import com.nexusai.domain.ai.AIProvider
import com.nexusai.domain.ai.AIResponse
import com.nexusai.domain.ai.ChatMessage
import com.nexusai.domain.model.MessageRole
import com.nexusai.domain.ai.TokenUsage
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class AnthropicProvider(
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val baseUrl: String = "https://api.anthropic.com/v1"
) : AIProvider {

    override val id = "anthropic"
    override val name = "Anthropic"
    override val type = "ANTHROPIC"
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
        val systemMessage = messages.firstOrNull { it.role == MessageRole.SYSTEM }
        val chatMessages = messages.filter { it.role != MessageRole.SYSTEM }

        val request = AnthropicRequest(
            model = model,
            messages = chatMessages.map { it.toAnthropic() },
            maxTokens = maxTokens,
            temperature = temperature,
            system = systemMessage?.content,
            stream = false
        )

        val response = httpClient.post("$baseUrl/messages") {
            contentType(ContentType.Application.Json)
            header("x-api-key", apiKey)
            header("anthropic-version", "2023-06-01")
            setBody(json.encodeToString(AnthropicRequest.serializer(), request))
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw Exception("Anthropic API error: ${response.status} - $errorBody")
        }

        val responseBody = response.bodyAsText()
        val anthropicResponse = json.decodeFromString(AnthropicResponse.serializer(), responseBody)

        val content = anthropicResponse.content.firstOrNull { it.type == "text" }
            ?: throw Exception("No text response from Anthropic")

        return AIResponse(
            content = content.text,
            model = anthropicResponse.model,
            usage = anthropicResponse.usage?.let {
                TokenUsage(
                    promptTokens = it.inputTokens,
                    completionTokens = it.outputTokens,
                    totalTokens = it.inputTokens + it.outputTokens
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
        val systemMessage = messages.firstOrNull { it.role == MessageRole.SYSTEM }
        val chatMessages = messages.filter { it.role != MessageRole.SYSTEM }

        val request = AnthropicRequest(
            model = model,
            messages = chatMessages.map { it.toAnthropic() },
            maxTokens = maxTokens,
            temperature = temperature,
            system = systemMessage?.content,
            stream = true
        )

        val requestJson = json.encodeToString(AnthropicRequest.serializer(), request)

        withContext(Dispatchers.IO) {
            val url = URL("$baseUrl/messages")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("x-api-key", apiKey)
            connection.setRequestProperty("anthropic-version", "2023-06-01")
            connection.setRequestProperty("Accept", "text/event-stream")
            connection.doOutput = true
            connection.connectTimeout = 30_000
            connection.readTimeout = 60_000

            connection.outputStream.use { os ->
                os.write(requestJson.toByteArray())
            }

            if (connection.responseCode != 200) {
                val error = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                throw Exception("Anthropic API error: ${connection.responseCode} - $error")
            }

            BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line ?: continue
                    if (currentLine.startsWith("data: ")) {
                        val data = currentLine.removePrefix("data: ").trim()
                        if (data.isEmpty()) continue

                        try {
                            val event = json.decodeFromString(AnthropicStreamEvent.serializer(), data)
                            if (event.type == "content_block_delta" && event.delta?.type == "text_delta") {
                                event.delta.text?.let { emit(it) }
                            }
                        } catch (e: Exception) {
                            // Skip malformed chunks
                        }
                    }
                }
            }

            connection.disconnect()
        }
    }

    override suspend fun listModels(): List<String> {
        return listOf(
            "claude-sonnet-4-20250514",
            "claude-3-5-sonnet-20241022",
            "claude-3-5-haiku-20241022",
            "claude-3-opus-20240229"
        )
    }

    private fun ChatMessage.toAnthropic() = AnthropicMessage(
        role = when (role) {
            MessageRole.SYSTEM -> "user"
            MessageRole.USER -> "user"
            MessageRole.ASSISTANT -> "assistant"
        },
        content = content
    )
}
