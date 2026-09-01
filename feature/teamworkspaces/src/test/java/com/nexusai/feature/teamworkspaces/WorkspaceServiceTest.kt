package com.nexusai.feature.teamworkspaces

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkspaceServiceTest {

    private lateinit var service: WorkspaceService
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
        service = WorkspaceService()
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `createDemoWorkspace returns workspace with members`() = runTest {
        val workspace = service.createDemoWorkspace()
        assertNotNull(workspace)
        assertEquals("Демо workspace", workspace.name)
        assertEquals(3, workspace.members.size)
    }

    @Test
    fun `createDemoWorkspace has correct owner`() = runTest {
        val workspace = service.createDemoWorkspace()
        val owner = workspace.members.find { it.role == MemberRole.OWNER }
        assertNotNull(owner)
        assertEquals("Вы", owner?.name)
    }

    @Test
    fun `getDemoMessages returns messages`() = runTest {
        val messages = service.getDemoMessages()
        assertTrue(messages.isNotEmpty())
    }

    @Test
    fun `getDemoMessages includes system message`() = runTest {
        val messages = service.getDemoMessages()
        val systemMessage = messages.find { it.type == MessageType.SYSTEM }
        assertNotNull(systemMessage)
    }

    @Test
    fun `members flow is updated after createDemoWorkspace`() = runTest {
        service.createDemoWorkspace()
        val members = service.members.first()
        assertEquals(3, members.size)
    }

    @Test
    fun `messages flow is updated after getDemoMessages`() = runTest {
        service.getDemoMessages()
        val messages = service.messages.first()
        assertTrue(messages.isNotEmpty())
    }

    @Test
    fun `sendMessage adds message to flow`() = runTest {
        service.sendMessage("Hello", "user_1", "Test User")
        val messages = service.messages.first()
        assertTrue(messages.any { it.content == "Hello" })
    }

    @Test
    fun `addMember adds member to flow`() = runTest {
        val member = WorkspaceMember(
            id = "new_member",
            name = "New Member",
            role = MemberRole.VIEWER
        )
        service.addMember(member)
        val members = service.members.first()
        assertTrue(members.any { it.id == "new_member" })
    }

    @Test
    fun `removeMember removes member from flow`() = runTest {
        service.createDemoWorkspace()
        service.removeMember("user_2")
        val members = service.members.first()
        assertFalse(members.any { it.id == "user_2" })
    }

    @Test
    fun `updateMemberStatus updates online status`() = runTest {
        service.createDemoWorkspace()
        service.updateMemberStatus("user_3", true)
        val members = service.members.first()
        val bob = members.find { it.id == "user_3" }
        assertTrue(bob?.isOnline ?: false)
    }

    @Test
    fun `disconnect clears messages and members`() = runTest {
        service.createDemoWorkspace()
        service.getDemoMessages()
        service.disconnect()
        val messages = service.messages.first()
        val members = service.members.first()
        assertTrue(messages.isEmpty())
        assertTrue(members.isEmpty())
    }

    @Test
    fun `Workspace has correct default values`() {
        val workspace = Workspace(
            id = "test",
            name = "Test",
            ownerId = "user_1"
        )
        assertEquals("", workspace.description)
        assertTrue(workspace.members.isEmpty())
        assertTrue(workspace.isActive)
    }

    @Test
    fun `WorkspaceMember has correct default values`() {
        val member = WorkspaceMember(
            id = "test",
            name = "Test"
        )
        assertEquals(MemberRole.VIEWER, member.role)
        assertFalse(member.isOnline)
    }

    @Test
    fun `MemberRole has correct display names`() {
        assertEquals("Владелец", MemberRole.OWNER.displayName)
        assertEquals("Администратор", MemberRole.ADMIN.displayName)
        assertEquals("Редактор", MemberRole.EDITOR.displayName)
        assertEquals("Наблюдатель", MemberRole.VIEWER.displayName)
    }

    @Test
    fun `MessageType has correct display names`() {
        assertEquals("Текст", MessageType.TEXT.displayName)
        assertEquals("Код", MessageType.CODE.displayName)
        assertEquals("Файл", MessageType.FILE.displayName)
        assertEquals("Система", MessageType.SYSTEM.displayName)
    }
}
