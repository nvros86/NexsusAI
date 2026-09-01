package com.nexusai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryEntryDao {
    @Query("SELECT * FROM memory_entries ORDER BY isImportant DESC, createdAt DESC")
    fun getAllEntries(): Flow<List<MemoryEntryEntity>>

    @Query("SELECT * FROM memory_entries WHERE id = :id")
    suspend fun getEntryById(id: String): MemoryEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MemoryEntryEntity)

    @Update
    suspend fun updateEntry(entry: MemoryEntryEntity)

    @Query("DELETE FROM memory_entries WHERE id = :id")
    suspend fun deleteEntryById(id: String)

    @Query("DELETE FROM memory_entries")
    suspend fun deleteAllEntries()
}
