package com.nexusai.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TabDao {
    @Query("SELECT * FROM tabs ORDER BY createdAt DESC")
    fun getAllTabs(): Flow<List<TabEntity>>

    @Query("SELECT * FROM tabs WHERE id = :id")
    suspend fun getTabById(id: String): TabEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTab(tab: TabEntity)

    @Update
    suspend fun updateTab(tab: TabEntity)

    @Delete
    suspend fun deleteTab(tab: TabEntity)

    @Query("DELETE FROM tabs WHERE id = :id")
    suspend fun deleteTabById(id: String)

    @Query("UPDATE tabs SET isActive = 0")
    suspend fun deactivateAllTabs()

    @Query("UPDATE tabs SET isActive = 1 WHERE id = :id")
    suspend fun setActiveTab(id: String)
}
