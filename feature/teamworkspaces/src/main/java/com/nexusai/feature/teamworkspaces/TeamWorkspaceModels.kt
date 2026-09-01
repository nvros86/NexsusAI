package com.nexusai.feature.teamworkspaces

import kotlinx.serialization.Serializable

@Serializable
data class Workspace(
    val id: String,
    val name: String,
    val description: String = "",
    val ownerId: String,
    val members: List<WorkspaceMember> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Serializable
data class WorkspaceMember(
    val id: String,
    val name: String,
    val role: MemberRole = MemberRole.VIEWER,
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
)

@Serializable
enum class MemberRole(val displayName: String) {
    OWNER("Владелец"),
    ADMIN("Администратор"),
    EDITOR("Редактор"),
    VIEWER("Наблюдатель")
}

@Serializable
data class WorkspaceMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT
)

@Serializable
enum class MessageType(val displayName: String) {
    TEXT("Текст"),
    CODE("Код"),
    FILE("Файл"),
    SYSTEM("Система")
}

@Serializable
data class SharedTab(
    val id: String,
    val workspaceId: String,
    val title: String,
    val content: String = "",
    val lastEditedBy: String = "",
    val lastEditedAt: Long = System.currentTimeMillis()
)

data class WorkspaceStatus(
    val isConnected: Boolean = false,
    val membersOnline: Int = 0,
    val lastSync: Long = 0,
    val error: String? = null
)
