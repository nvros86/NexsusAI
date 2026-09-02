package com.nexusai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TabEntity::class, AIProviderEntity::class, AgentEntity::class, MemoryEntryEntity::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tabDao(): TabDao
    abstract fun aiProviderDao(): AIProviderDao
    abstract fun agentDao(): AgentDao
    abstract fun memoryEntryDao(): MemoryEntryDao

    companion object {
        const val CURRENT_VERSION = 3
    }
}
