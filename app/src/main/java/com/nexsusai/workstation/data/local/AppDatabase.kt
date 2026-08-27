package com.nexsusai.workstation.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WorkSessionEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workSessionDao(): WorkSessionDao
}
