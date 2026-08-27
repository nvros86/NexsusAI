package com.nexsusai.workstation.ai.network

import kotlinx.coroutines.flow.Flow

/**
 * Streaming HTTP abstraction for AI providers.
 * Implementations will use Ktor Client and SSE parsing.
 */
interface StreamingHttpClient {
    suspend fun stream(request: String): Flow<String>
}
