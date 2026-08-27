package com.nexsusai.workstation.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkSessionDao {
    @Query("SELECT * FROM work_sessions ORDER BY createdAt ASC")
    fun observeSessions(): Flow<List<WorkSessionEntity>>

    @Insert
    suspend fun insert(session: WorkSessionEntity)

    @Delete
    suspend fun delete(session: WorkSessionEntity)
}
