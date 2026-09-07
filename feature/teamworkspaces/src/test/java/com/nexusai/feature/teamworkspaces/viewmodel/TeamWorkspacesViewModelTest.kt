package com.nexusai.feature.teamworkspaces.viewmodel

import com.nexusai.feature.teamworkspaces.MemberRole
import com.nexusai.feature.teamworkspaces.Workspace
import com.nexusai.feature.teamworkspaces.WorkspaceMember
import com.nexusai.feature.teamworkspaces.WorkspaceMessage
import com.nexusai.feature.teamworkspaces.WorkspaceService
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TeamWorkspacesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var workspaceService: WorkspaceService
    private lateinit var viewModel: TeamWorkspacesViewModel

    private val membersFlow = MutableStateFlow<List<WorkspaceMember>>(emptyList())
    private val messagesFlow = MutableStateFlow<List<WorkspaceMessage>>(emptyList())

    private fun createWorkspace(
        id: String = "ws-1",
        name: String = "Test Workspace",
        description: String = "A test workspace"
    ) = Workspace(
        id = id,
        name = name,
        description = description,
        ownerId = "user_1"
    )

    private fun createMember(
        id: String = "user-1",
        name: String = "User",
        role: MemberRole = MemberRole.VIEWER
    ) = WorkspaceMember(
        id = id,
        name = name,
        role = role
    )

    private fun createMessage(
        id: String = "msg-1",
        content: String = "Hello",
        senderId: String = "user-1",
        senderName: String = "User"
    ) = WorkspaceMessage(
        id = id,
        senderId = senderId,
        senderName = senderName,
        content = content
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        workspaceService = mockk(relaxed = true)
        every { workspaceService.members } returns membersFlow
        every { workspaceService.messages } returns messagesFlow
        coEvery { workspaceService.createDemoWorkspace() } returns createWorkspace()
        coEvery { workspaceService.getDemoMessages() } returns emptyList()
        viewModel = TeamWorkspacesViewModel(workspaceService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        val state = viewModel.uiState.value
        assertNotNull(state.workspaces)
        assertNull(state.selectedWorkspace)
        assertTrue(state.members.isEmpty())
        assertTrue(state.messages.isEmpty())
        assertFalse(state.isConnecting)
        assertNull(state.error)
    }

    @Test
    fun `init creates demo workspace`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.workspaces.size)
        assertEquals("Test Workspace", state.workspaces[0].name)
    }

    @Test
    fun `createWorkspace adds workspace to list`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.createWorkspace("New Project", "Project description")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.workspaces.size)
        assertEquals("New Project", state.workspaces[1].name)
        assertEquals("Project description", state.workspaces[1].description)
    }

    @Test
    fun `createWorkspace sets owner correctly`() = runTest {
        viewModel.createWorkspace("My Workspace", "desc")
        testDispatcher.scheduler.advanceUntilIdle()

        val workspace = viewModel.uiState.value.workspaces.last()
        assertEquals("user_1", workspace.ownerId)
        assertEquals(1, workspace.members.size)
        assertEquals(MemberRole.OWNER, workspace.members[0].role)
    }

    @Test
    fun `selectWorkspace updates selectedWorkspace`() = runTest {
        val workspace = createWorkspace(id = "ws-1", name = "My Workspace")
        viewModel.selectWorkspace(workspace)

        assertEquals(workspace, viewModel.uiState.value.selectedWorkspace)
        assertEquals("My Workspace", viewModel.uiState.value.selectedWorkspace?.name)
    }

    @Test
    fun `clearSelectedWorkspace resets selectedWorkspace to null`() = runTest {
        viewModel.selectWorkspace(createWorkspace())
        assertNotNull(viewModel.uiState.value.selectedWorkspace)

        viewModel.clearSelectedWorkspace()
        assertNull(viewModel.uiState.value.selectedWorkspace)
    }

    @Test
    fun `sendMessage calls workspaceService`() = runTest {
        viewModel.selectWorkspace(createWorkspace())
        viewModel.sendMessage("Hello, team!")
        testDispatcher.scheduler.advanceUntilIdle()

        verify {
            workspaceService.sendMessage(
                content = "Hello, team!",
                senderId = "user_1",
                senderName = "Вы"
            )
        }
    }

    @Test
    fun `members flow updates are reflected in state`() = runTest {
        assertTrue(viewModel.uiState.value.members.isEmpty())

        membersFlow.value = listOf(createMember(id = "u1", name = "Alice"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.members.size)

        membersFlow.value = listOf(
            createMember(id = "u1", name = "Alice"),
            createMember(id = "u2", name = "Bob")
        )
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.members.size)
    }

    @Test
    fun `messages flow updates are reflected in state`() = runTest {
        assertTrue(viewModel.uiState.value.messages.isEmpty())

        messagesFlow.value = listOf(createMessage(id = "m1", content = "Hello"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.messages.size)
        assertEquals("Hello", viewModel.uiState.value.messages[0].content)
    }

    @Test
    fun `refresh reloads demo workspace`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val sizeBefore = viewModel.uiState.value.workspaces.size

        val newWorkspace = createWorkspace(id = "new-ws", name = "Refreshed Workspace")
        coEvery { workspaceService.createDemoWorkspace() } returns newWorkspace

        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.workspaces.size)
        assertEquals("Refreshed Workspace", state.workspaces[0].name)
    }

}
