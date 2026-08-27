package com.nexusai.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AIProviderDao {
    @Query("SELECT * FROM ai_providers ORDER BY name ASC")
    fun getAllProviders(): Flow<List<AIProviderEntity>>

    @Query("SELECT * FROM ai_providers WHERE id = :id")
    suspend fun getProviderById(id: String): AIProviderEntity?

    @Query("SELECT * FROM ai_providers WHERE isFavorite = 1")
    suspend fun getFavoriteProviders(): List<AIProviderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: AIProviderEntity)

    @Update
    suspend fun updateProvider(provider: AIProviderEntity)

    @Delete
    suspend fun deleteProvider(provider: AIProviderEntity)

    @Query("DELETE FROM ai_providers WHERE id = :id")
    suspend fun deleteProviderById(id: String)
}
