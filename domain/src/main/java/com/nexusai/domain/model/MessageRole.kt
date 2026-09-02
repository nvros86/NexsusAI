package com.nexusai.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class MessageRole {
    SYSTEM,
    USER,
    ASSISTANT
}
