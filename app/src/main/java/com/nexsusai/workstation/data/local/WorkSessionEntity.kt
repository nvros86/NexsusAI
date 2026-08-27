package com.nexsusai.workstation.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_sessions")
data class WorkSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val aiProvider: String,
    val context: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
