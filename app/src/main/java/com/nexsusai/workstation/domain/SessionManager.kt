package com.nexsusai.workstation.domain

class SessionManager {
    private val sessions = mutableListOf<WorkSession>()

    fun createSession(title: String): WorkSession {
        val session = WorkSession(title = title)
        sessions.add(session)
        return session
    }

    fun removeSession(id: String) {
        sessions.removeIf { it.id == id }
    }

    fun getSessions(): List<WorkSession> = sessions.toList()
}
