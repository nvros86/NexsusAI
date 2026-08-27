package com.nexusai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TabEntity::class, AIProviderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tabDao(): TabDao
    abstract fun aiProviderDao(): AIProviderDao
}
