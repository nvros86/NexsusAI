package com.nexusai.domain.repository

import com.nexusai.domain.model.SplitSession
import kotlinx.coroutines.flow.Flow

interface SplitViewRepository {
    fun getAllSessions(): Flow<List<SplitSession>>
    suspend fun getSession(id: String): SplitSession?
    suspend fun saveSession(session: SplitSession)
    suspend fun deleteSession(id: String)
}
