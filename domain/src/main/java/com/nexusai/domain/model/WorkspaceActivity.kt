package com.nexusai.domain.model

data class WorkspaceActivity(
    val id: String,
    val workspaceId: String,
    val memberId: String,
    val memberName: String,
    val action: ActivityAction,
    val targetName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class ActivityAction {
    CREATED_WORKSPACE,
    ADDED_MEMBER,
    REMOVED_MEMBER,
    CHANGED_ROLE,
    CREATED_TAB,
    SENT_MESSAGE,
    UPLOADED_FILE,
    RAN_CHAIN
}
