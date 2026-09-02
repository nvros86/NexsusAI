package com.nexusai.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Define migration SQL here.
        // Example: db.execSQL("ALTER TABLE tabs ADD COLUMN newColumn TEXT NOT NULL DEFAULT ''")
    }
}

val ALL_MIGRATIONS = arrayOf(MIGRATION_3_4)
