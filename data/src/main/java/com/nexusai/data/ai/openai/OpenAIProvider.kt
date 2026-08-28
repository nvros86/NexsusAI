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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

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
            model = openAIResponse.id,
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

        val requestJson = json.encodeToString(OpenAIRequest.serializer(), request)

        withContext(Dispatchers.IO) {
            val url = URL("$baseUrl/chat/completions")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.setRequestProperty("Accept", "text/event-stream")
            connection.doOutput = true
            connection.connectTimeout = 30_000
            connection.readTimeout = 60_000

            connection.outputStream.use { os ->
                os.write(requestJson.toByteArray())
            }

            if (connection.responseCode != 200) {
                val error = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                throw Exception("OpenAI API error: ${connection.responseCode} - $error")
            }

            BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line ?: continue
                    if (currentLine.startsWith("data: ")) {
                        val data = currentLine.removePrefix("data: ").trim()
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

            connection.disconnect()
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
