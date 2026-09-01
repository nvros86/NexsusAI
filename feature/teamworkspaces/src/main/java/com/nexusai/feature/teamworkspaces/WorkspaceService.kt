package com.nexusai.feature.teamworkspaces

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class WebSocketMessage(
    val type: String,
    val payload: String
)

@Singleton
class WorkspaceService @Inject constructor() {

    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val _messages = MutableStateFlow<List<WorkspaceMessage>>(emptyList())
    val messages: StateFlow<List<WorkspaceMessage>> = _messages.asStateFlow()

    private val _members = MutableStateFlow<List<WorkspaceMember>>(emptyList())
    val members: StateFlow<List<WorkspaceMember>> = _members.asStateFlow()

    private val _status = MutableStateFlow(WorkspaceStatus())
    val status: StateFlow<WorkspaceStatus> = _status.asStateFlow()

    suspend fun connect(workspaceId: String, serverUrl: String): Boolean {
        return try {
            _status.value = WorkspaceStatus(isConnected = true)
            true
        } catch (e: Exception) {
            _status.value = WorkspaceStatus(error = e.message)
            false
        }
    }

    fun disconnect() {
        _status.value = WorkspaceStatus(isConnected = false)
        _messages.value = emptyList()
        _members.value = emptyList()
    }

    fun sendMessage(content: String, senderId: String, senderName: String) {
        val message = WorkspaceMessage(
            id = System.currentTimeMillis().toString(),
            senderId = senderId,
            senderName = senderName,
            content = content
        )
        _messages.value = _messages.value + message
    }

    fun addMember(member: WorkspaceMember) {
        _members.value = _members.value + member
    }

    fun removeMember(memberId: String) {
        _members.value = _members.value.filter { it.id != memberId }
    }

    fun updateMemberStatus(memberId: String, isOnline: Boolean) {
        _members.value = _members.value.map {
            if (it.id == memberId) it.copy(isOnline = isOnline) else it
        }
    }

    fun createDemoWorkspace(): Workspace {
        val members = listOf(
            WorkspaceMember(
                id = "user_1",
                name = "Вы",
                role = MemberRole.OWNER,
                isOnline = true
            ),
            WorkspaceMember(
                id = "user_2",
                name = "Алиса",
                role = MemberRole.EDITOR,
                isOnline = true
            ),
            WorkspaceMember(
                id = "user_3",
                name = "Боб",
                role = MemberRole.VIEWER,
                isOnline = false
            )
        )
        _members.value = members

        return Workspace(
            id = "demo_workspace",
            name = "Демо workspace",
            description = "Для тестирования совместной работы",
            ownerId = "user_1",
            members = members
        )
    }

    fun getDemoMessages(): List<WorkspaceMessage> {
        val messages = listOf(
            WorkspaceMessage(
                id = "1",
                senderId = "user_2",
                senderName = "Алиса",
                content = "Привет! Я только что подключилась к workspace",
                timestamp = System.currentTimeMillis() - 300000
            ),
            WorkspaceMessage(
                id = "2",
                senderId = "user_1",
                senderName = "Вы",
                content = "Добро пожаловать! Давай начнём работу",
                timestamp = System.currentTimeMillis() - 240000
            ),
            WorkspaceMessage(
                id = "3",
                senderId = "user_2",
                senderName = "Алиса",
                content = "Отлично! Я вижу общий чат и вкладки",
                timestamp = System.currentTimeMillis() - 180000
            ),
            WorkspaceMessage(
                id = "4",
                senderId = "system",
                senderName = "Система",
                content = "Боб присоединился к workspace",
                timestamp = System.currentTimeMillis() - 120000,
                type = MessageType.SYSTEM
            )
        )
        _messages.value = messages
        return messages
    }
}
