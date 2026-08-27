package com.nexsusai.workstation.ai

interface AIProvider {
    val id: String
    val name: String

    suspend fun sendMessage(
        message: String,
        context: String?
    ): String
}
