package com.nexsusai.workstation.data

import com.nexsusai.workstation.data.local.WorkSessionDao
import com.nexsusai.workstation.data.local.WorkSessionEntity
import kotlinx.coroutines.flow.Flow

class SessionRepository(
    private val dao: WorkSessionDao
) {
    fun observeSessions(): Flow<List<WorkSessionEntity>> = dao.observeSessions()

    suspend fun saveSession(session: WorkSessionEntity) {
        dao.insert(session)
    }

    suspend fun removeSession(id: String) {
        dao.delete(id)
    }
}
