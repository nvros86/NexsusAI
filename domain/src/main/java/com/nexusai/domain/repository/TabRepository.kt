package com.nexusai.domain.repository

import com.nexusai.domain.model.Tab
import kotlinx.coroutines.flow.Flow

interface TabRepository {
    fun getAllTabs(): Flow<List<Tab>>
    fun searchTabs(query: String): Flow<List<Tab>>
    suspend fun getTabById(id: String): Tab?
    suspend fun createTab(tab: Tab): Tab
    suspend fun updateTab(tab: Tab)
    suspend fun deleteTab(id: String)
    suspend fun setActiveTab(id: String)
}
