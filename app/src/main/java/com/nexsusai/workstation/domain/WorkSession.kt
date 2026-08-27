package com.nexsusai.workstation.domain

import java.util.UUID

data class WorkSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val modelProvider: String? = null,
    val context: String = ""
)
