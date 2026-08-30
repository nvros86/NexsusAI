package com.nexusai.domain.model

import java.util.UUID

data class SplitSession(
    val id: String = UUID.randomUUID().toString(),
    val query: String,
    val results: List<SplitResult> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val selectedWinner: String? = null
)

data class SplitResult(
    val providerId: String,
    val providerName: String,
    val modelName: String,
    val response: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val latencyMs: Long = 0,
    val tokensUsed: Int = 0,
    val rating: Int = 0
)

enum class ComparisonMode(val displayName: String, val count: Int) {
    TWO("2 AI", 2),
    THREE("3 AI", 3),
    FOUR("4 AI", 4)
}
