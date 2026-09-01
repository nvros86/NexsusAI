package com.nexusai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_entries")
data class MemoryEntryEntity(
    @PrimaryKey val id: String,
    val key: String,
    val value: String,
    val isImportant: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
