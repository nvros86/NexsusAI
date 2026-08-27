package com.nexusai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabs")
data class TabEntity(
    @PrimaryKey val id: String,
    val title: String,
    val aiProviderId: String?,
    val messagesJson: String,
    val attachedFilesJson: String,
    val isActive: Boolean,
    val createdAt: Long,
    val accentColor: Long
)
