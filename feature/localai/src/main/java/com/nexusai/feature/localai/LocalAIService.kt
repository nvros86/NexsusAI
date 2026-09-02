package com.nexusai.feature.localai

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class OllamaTagsResponse(
    val models: List<OllamaModel> = emptyList()
)

@Serializable
private data class OllamaModel(
    val name: String = "",
    val size: Long = 0,
    val digest: String = ""
)

@Serializable
private data class OllamaGenerateRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false
)

@Serializable
private data class OllamaGenerateResponse(
    val response: String = "",
    val done: Boolean = false
)

@Singleton
class LocalAIService @Inject constructor(
    private val httpClient: HttpClient
) {

    suspend fun checkConnection(baseUrl: String): Boolean {
        return try {
            httpClient.get("$baseUrl/api/tags").status.value == 200
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getModels(baseUrl: String): List<LocalAIModel> {
        return try {
            val response = httpClient.get("$baseUrl/api/tags")
            val tagsResponse = response.body<OllamaTagsResponse>()
            tagsResponse.models.map { model ->
                LocalAIModel(
                    id = model.name,
                    name = model.name,
                    size = model.size,
                    parameterSize = extractParameterSize(model.name),
                    quantization = extractQuantization(model.name)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun generate(
        baseUrl: String,
        model: String,
        prompt: String,
        systemPrompt: String = ""
    ): String {
        return try {
            val fullPrompt = if (systemPrompt.isNotEmpty()) {
                "System: $systemPrompt\n\nUser: $prompt"
            } else {
                prompt
            }

            val response = httpClient.post("$baseUrl/api/generate") {
                contentType(ContentType.Application.Json)
                setBody(
                    OllamaGenerateRequest(
                        model = model,
                        prompt = fullPrompt,
                        stream = false
                    )
                )
            }

            val generateResponse = response.body<OllamaGenerateResponse>()
            generateResponse.response
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    suspend fun pullModel(baseUrl: String, modelName: String): Boolean {
        return try {
            httpClient.post("$baseUrl/api/pull") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("name" to modelName))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteModel(baseUrl: String, modelName: String): Boolean {
        return try {
            httpClient.post("$baseUrl/api/delete") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("name" to modelName))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun extractParameterSize(modelName: String): String {
        val regex = Regex("(\\d+\\.?\\d*)[bB]")
        return regex.find(modelName)?.groupValues?.get(1)?.let { "${it}B" } ?: ""
    }

    private fun extractQuantization(modelName: String): String {
        val quantPatterns = listOf("Q4_0", "Q4_1", "Q4_K_M", "Q4_K_S", "Q5_0", "Q5_1", "Q5_K_M", "Q5_K_S", "Q6_K", "Q8_0", "F16", "F32")
        return quantPatterns.firstOrNull { modelName.contains(it, ignoreCase = true) } ?: ""
    }
}
