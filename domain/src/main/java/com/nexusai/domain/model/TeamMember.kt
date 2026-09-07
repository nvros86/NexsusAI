package com.nexusai.domain.model

data class TeamMember(
    val id: String,
    val name: String,
    val email: String = "",
    val avatarUrl: String = "",
    val role: TeamMemberRole = TeamMemberRole.VIEWER,
    val isOnline: Boolean = false,
    val lastActiveAt: Long = System.currentTimeMillis()
)

enum class TeamMemberRole(val displayName: String) {
    OWNER("Owner"),
    ADMIN("Admin"),
    EDITOR("Editor"),
    VIEWER("Viewer")
}
