package com.nexusai.core.common

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResponseCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("response_cache", Context.MODE_PRIVATE)
    }

    private val json = Json { ignoreUnknownKeys = true }

    fun cacheResponse(key: String, response: String, model: String) {
        val cached = CachedResponse(
            response = response,
            model = model,
            timestamp = System.currentTimeMillis()
        )
        prefs.edit().putString(key, json.encodeToString(cached)).apply()
    }

    fun getCachedResponse(key: String, maxAgeMs: Long = 3600000): String? {
        val cached = prefs.getString(key, null) ?: return null
        return try {
            val response = json.decodeFromString<CachedResponse>(cached)
            if (System.currentTimeMillis() - response.timestamp < maxAgeMs) {
                response.response
            } else {
                prefs.edit().remove(key).apply()
                null
            }
        } catch (e: Exception) {
            prefs.edit().remove(key).apply()
            null
        }
    }

    fun clearCache() {
        prefs.edit().clear().apply()
    }

    fun removeCached(key: String) {
        prefs.edit().remove(key).apply()
    }
}

@kotlinx.serialization.Serializable
private data class CachedResponse(
    val response: String,
    val model: String,
    val timestamp: Long
)
