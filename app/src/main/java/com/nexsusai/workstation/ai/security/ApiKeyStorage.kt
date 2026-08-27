package com.nexsusai.workstation.ai.security

interface ApiKeyStorage {
    suspend fun save(provider: String, key: String)
    suspend fun get(provider: String): String?
    suspend fun remove(provider: String)
}
