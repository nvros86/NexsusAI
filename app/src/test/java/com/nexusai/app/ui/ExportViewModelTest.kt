package com.nexusai.app.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.nexusai.app.R
import com.nexusai.domain.model.AttachedFile
import com.nexusai.domain.model.Message
import com.nexusai.domain.model.MessageRole
import com.nexusai.domain.model.Tab
import com.nexusai.domain.repository.TabRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class ExportViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockApplication = mockk<Application>(relaxed = true)
    private lateinit var tabRepository: TabRepository
    private lateinit var viewModel: ExportViewModel

    private fun createMessage(
        id: String = "msg-1",
        content: String = "Hello, world!",
        role: MessageRole = MessageRole.USER,
        timestamp: Long = 1000L,
        attachments: List<AttachedFile> = emptyList()
    ) = Message(
        id = id,
        content = content,
        role = role,
        timestamp = timestamp,
        attachments = attachments
    )

    private fun createTab(
        id: String = "tab-1",
        title: String = "Test Tab",
        aiProviderId: String? = "provider-1",
        messages: List<Message> = listOf(
            createMessage(content = "Hello"),
            createMessage(id = "msg-2", content = "Hi there!", role = MessageRole.ASSISTANT)
        )
    ) = Tab(
        id = id,
        title = title,
        aiProviderId = aiProviderId,
        messages = messages
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        tabRepository = mockk(relaxed = true)
        every { tabRepository.getAllTabs() } returns flowOf(emptyList())
        viewModel = ExportViewModel(mockApplication, tabRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        val state = viewModel.uiState.value
        assertTrue(state.tabs.isEmpty())
        assertNull(state.selectedTabId)
        assertEquals(ExportFormat.MARKDOWN, state.selectedFormat)
        assertFalse(state.isExporting)
        assertNull(state.exportedFileUri)
        assertNull(state.error)
    }

    @Test
    fun `init loads tabs from repository`() = runTest {
        val tabs = listOf(createTab(id = "1", title = "Tab A"), createTab(id = "2", title = "Tab B"))
        every { tabRepository.getAllTabs() } returns flowOf(tabs)
        val vm = ExportViewModel(mockApplication, tabRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(2, state.tabs.size)
        assertEquals("Tab A", state.tabs[0].title)
        assertEquals("Tab B", state.tabs[1].title)
    }

    @Test
    fun `selectTab updates selectedTabId`() {
        viewModel.selectTab("tab-42")
        assertEquals("tab-42", viewModel.uiState.value.selectedTabId)
    }

    @Test
    fun `selectTab can be called multiple times`() {
        viewModel.selectTab("tab-1")
        viewModel.selectTab("tab-2")
        assertEquals("tab-2", viewModel.uiState.value.selectedTabId)
    }

    @Test
    fun `selectFormat updates selectedFormat`() {
        viewModel.selectFormat(ExportFormat.JSON)
        assertEquals(ExportFormat.JSON, viewModel.uiState.value.selectedFormat)

        viewModel.selectFormat(ExportFormat.HTML)
        assertEquals(ExportFormat.HTML, viewModel.uiState.value.selectedFormat)
    }

    @Test
    fun `selectFormat all formats are selectable`() {
        for (format in ExportFormat.entries) {
            viewModel.selectFormat(format)
            assertEquals(format, viewModel.uiState.value.selectedFormat)
        }
    }

    @Test
    fun `clearExport resets exportedFileUri`() {
        viewModel.clearExport()
        assertNull(viewModel.uiState.value.exportedFileUri)
    }

    @Test
    fun `copyToClipboard generates markdown for selected tab`() = runTest {
        val tab = createTab(
            id = "tab-1",
            title = "My Chat",
            messages = listOf(
                createMessage(content = "Hello"),
                createMessage(id = "msg-2", content = "Hi there!", role = MessageRole.ASSISTANT)
            )
        )
        every { tabRepository.getAllTabs() } returns flowOf(listOf(tab))
        val vm = ExportViewModel(mockApplication, tabRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectTab("tab-1")
        vm.selectFormat(ExportFormat.MARKDOWN)

        mockkStatic(android.content.ClipData::class)
        val contentSlot = slot<String>()
        val clipData = mockk<ClipData>(relaxed = true)
        every { ClipData.newPlainText(any(), capture(contentSlot)) } returns clipData

        val context = mockk<Context>(relaxed = true)
        val clipboardManager = mockk<ClipboardManager>(relaxed = true)
        every { context.getSystemService(Context.CLIPBOARD_SERVICE) } returns clipboardManager

        vm.copyToClipboard(context)
        testDispatcher.scheduler.advanceUntilIdle()

        val content = contentSlot.captured
        assertTrue(content.contains("# My Chat"))
        assertTrue(content.contains("Hello"))
        assertTrue(content.contains("Hi there!"))
        assertTrue(content.contains("Пользователь"))
        assertTrue(content.contains("Ассистент"))

        verify { clipboardManager.setPrimaryClip(clipData) }
        unmockkStatic(android.content.ClipData::class)
    }

    @Test
    fun `copyToClipboard generates plain text format`() = runTest {
        val tab = createTab(id = "tab-1", title = "Plain Chat")
        every { tabRepository.getAllTabs() } returns flowOf(listOf(tab))
        val vm = ExportViewModel(mockApplication, tabRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectTab("tab-1")
        vm.selectFormat(ExportFormat.TXT)

        mockkStatic(android.content.ClipData::class)
        val contentSlot = slot<String>()
        val clipData = mockk<ClipData>(relaxed = true)
        every { ClipData.newPlainText(any(), capture(contentSlot)) } returns clipData

        val context = mockk<Context>(relaxed = true)
        val clipboardManager = mockk<ClipboardManager>(relaxed = true)
        every { context.getSystemService(Context.CLIPBOARD_SERVICE) } returns clipboardManager

        vm.copyToClipboard(context)
        testDispatcher.scheduler.advanceUntilIdle()

        val content = contentSlot.captured
        assertTrue(content.contains("Plain Chat"))
        assertTrue(content.contains("=".repeat("Plain Chat".length)))
        assertTrue(content.contains("[Пользователь]"))
        assertTrue(content.contains("[Ассистент]"))
        assertFalse(content.contains("#"))

        unmockkStatic(android.content.ClipData::class)
    }

    @Test
    fun `copyToClipboard generates JSON format`() = runTest {
        val tab = createTab(
            id = "tab-1",
            title = "JSON Chat",
            aiProviderId = "openai-1",
            messages = listOf(
                createMessage(content = "Question", role = MessageRole.USER, timestamp = 100L),
                createMessage(id = "msg-2", content = "Answer", role = MessageRole.ASSISTANT, timestamp = 200L)
            )
        )
        every { tabRepository.getAllTabs() } returns flowOf(listOf(tab))
        val vm = ExportViewModel(mockApplication, tabRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectTab("tab-1")
        vm.selectFormat(ExportFormat.JSON)

        mockkStatic(android.content.ClipData::class)
        val contentSlot = slot<String>()
        val clipData = mockk<ClipData>(relaxed = true)
        every { ClipData.newPlainText(any(), capture(contentSlot)) } returns clipData

        val context = mockk<Context>(relaxed = true)
        val clipboardManager = mockk<ClipboardManager>(relaxed = true)
        every { context.getSystemService(Context.CLIPBOARD_SERVICE) } returns clipboardManager

        vm.copyToClipboard(context)
        testDispatcher.scheduler.advanceUntilIdle()

        val content = contentSlot.captured
        assertTrue(content.contains("\"title\""))
        assertTrue(content.contains("JSON Chat"))
        assertTrue(content.contains("openai-1"))
        assertTrue(content.contains("\"role\""))
        assertTrue(content.contains("user"))
        assertTrue(content.contains("Question"))
        assertTrue(content.contains("assistant"))
        assertTrue(content.contains("Answer"))

        unmockkStatic(android.content.ClipData::class)
    }

    @Test
    fun `copyToClipboard generates HTML format`() = runTest {
        val tab = createTab(id = "tab-1", title = "HTML Chat")
        every { tabRepository.getAllTabs() } returns flowOf(listOf(tab))
        val vm = ExportViewModel(mockApplication, tabRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectTab("tab-1")
        vm.selectFormat(ExportFormat.HTML)

        mockkStatic(android.content.ClipData::class)
        val contentSlot = slot<String>()
        val clipData = mockk<ClipData>(relaxed = true)
        every { ClipData.newPlainText(any(), capture(contentSlot)) } returns clipData

        val context = mockk<Context>(relaxed = true)
        val clipboardManager = mockk<ClipboardManager>(relaxed = true)
        every { context.getSystemService(Context.CLIPBOARD_SERVICE) } returns clipboardManager

        vm.copyToClipboard(context)
        testDispatcher.scheduler.advanceUntilIdle()

        val content = contentSlot.captured
        assertTrue(content.contains("<!DOCTYPE html>"))
        assertTrue(content.contains("<html"))
        assertTrue(content.contains("<h1>HTML Chat</h1>"))
        assertTrue(content.contains("Пользователь"))
        assertTrue(content.contains("Ассистент"))
        assertTrue(content.contains("</html>"))

        unmockkStatic(android.content.ClipData::class)
    }

    @Test
    fun `copyToClipboard does nothing when no tab selected`() = runTest {
        val tab = createTab(id = "tab-1")
        every { tabRepository.getAllTabs() } returns flowOf(listOf(tab))
        val vm = ExportViewModel(mockApplication, tabRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // No tab selected
        mockkStatic(android.content.ClipData::class)
        val context = mockk<Context>(relaxed = true)
        val clipboardManager = mockk<ClipboardManager>(relaxed = true)
        every { context.getSystemService(Context.CLIPBOARD_SERVICE) } returns clipboardManager

        vm.copyToClipboard(context)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 0) { clipboardManager.setPrimaryClip(any()) }
        unmockkStatic(android.content.ClipData::class)
    }

    @Test
    fun `copyToClipboard does nothing when selected tab not in list`() = runTest {
        every { tabRepository.getAllTabs() } returns flowOf(emptyList())
        val vm = ExportViewModel(mockApplication, tabRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectTab("nonexistent-tab")

        mockkStatic(android.content.ClipData::class)
        val context = mockk<Context>(relaxed = true)
        val clipboardManager = mockk<ClipboardManager>(relaxed = true)
        every { context.getSystemService(Context.CLIPBOARD_SERVICE) } returns clipboardManager

        vm.copyToClipboard(context)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 0) { clipboardManager.setPrimaryClip(any()) }
        unmockkStatic(android.content.ClipData::class)
    }

    @Test
    fun `copyToClipboard handles attachments in markdown`() = runTest {
        val attachment = AttachedFile(
            id = "att-1",
            name = "photo.png",
            uri = "content://photo",
            mimeType = "image/png",
            size = 1024
        )
        val tab = createTab(
            id = "tab-1",
            title = "Chat With Attachments",
            messages = listOf(
                createMessage(content = "See attached", attachments = listOf(attachment))
            )
        )
        every { tabRepository.getAllTabs() } returns flowOf(listOf(tab))
        val vm = ExportViewModel(mockApplication, tabRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectTab("tab-1")
        vm.selectFormat(ExportFormat.MARKDOWN)

        mockkStatic(android.content.ClipData::class)
        val contentSlot = slot<String>()
        val clipData = mockk<ClipData>(relaxed = true)
        every { ClipData.newPlainText(any(), capture(contentSlot)) } returns clipData

        val context = mockk<Context>(relaxed = true)
        val clipboardManager = mockk<ClipboardManager>(relaxed = true)
        every { context.getSystemService(Context.CLIPBOARD_SERVICE) } returns clipboardManager

        vm.copyToClipboard(context)
        testDispatcher.scheduler.advanceUntilIdle()

        val content = contentSlot.captured
        assertTrue(content.contains("Вложения"))
        assertTrue(content.contains("photo.png"))

        unmockkStatic(android.content.ClipData::class)
    }

    @Test
    fun `copyToClipboard handles attachments in JSON`() = runTest {
        val attachment = AttachedFile(
            id = "att-1",
            name = "doc.pdf",
            uri = "content://doc",
            mimeType = "application/pdf",
            size = 2048
        )
        val tab = createTab(
            id = "tab-1",
            title = "JSON With Attachments",
            messages = listOf(
                createMessage(content = "See doc", attachments = listOf(attachment))
            )
        )
        every { tabRepository.getAllTabs() } returns flowOf(listOf(tab))
        val vm = ExportViewModel(mockApplication, tabRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectTab("tab-1")
        vm.selectFormat(ExportFormat.JSON)

        mockkStatic(android.content.ClipData::class)
        val contentSlot = slot<String>()
        val clipData = mockk<ClipData>(relaxed = true)
        every { ClipData.newPlainText(any(), capture(contentSlot)) } returns clipData

        val context = mockk<Context>(relaxed = true)
        val clipboardManager = mockk<ClipboardManager>(relaxed = true)
        every { context.getSystemService(Context.CLIPBOARD_SERVICE) } returns clipboardManager

        vm.copyToClipboard(context)
        testDispatcher.scheduler.advanceUntilIdle()

        val content = contentSlot.captured
        assertTrue(content.contains("\"attachments\""))
        assertTrue(content.contains("doc.pdf"))
        assertTrue(content.contains("application/pdf"))
        assertTrue(content.contains("2048"))

        unmockkStatic(android.content.ClipData::class)
    }

    @Test
    fun `openInBrowser starts activity with correct intent`() {
        val context = mockk<Context>(relaxed = true)
        viewModel.openInBrowser(context)

        // No URI set, so startActivity should not be called
        verify(exactly = 0) { context.startActivity(any<Intent>()) }
    }

    @Test
    fun `share does nothing when no exported file`() {
        val context = mockk<Context>(relaxed = true)
        viewModel.share(context)

        verify(exactly = 0) { context.startActivity(any<Intent>()) }
    }

    @Test
    fun `export format enum has correct properties`() {
        assertEquals(R.string.export_format_markdown, ExportFormat.MARKDOWN.displayNameRes)
        assertEquals(".md", ExportFormat.MARKDOWN.extension)
        assertEquals("text/markdown", ExportFormat.MARKDOWN.mimeType)

        assertEquals(".txt", ExportFormat.TXT.extension)
        assertEquals("text/plain", ExportFormat.TXT.mimeType)

        assertEquals(".json", ExportFormat.JSON.extension)
        assertEquals("application/json", ExportFormat.JSON.mimeType)

        assertEquals(".html", ExportFormat.HTML.extension)
        assertEquals("text/html", ExportFormat.HTML.mimeType)
    }

    @Test
    fun `markdown format escapes HTML tags`() = runTest {
        val tab = createTab(
            id = "tab-1",
            title = "XSS Test",
            messages = listOf(
                createMessage(content = "<script>alert('xss')</script>")
            )
        )
        every { tabRepository.getAllTabs() } returns flowOf(listOf(tab))
        val vm = ExportViewModel(mockApplication, tabRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectTab("tab-1")
        vm.selectFormat(ExportFormat.HTML)

        mockkStatic(android.content.ClipData::class)
        val contentSlot = slot<String>()
        val clipData = mockk<ClipData>(relaxed = true)
        every { ClipData.newPlainText(any(), capture(contentSlot)) } returns clipData

        val context = mockk<Context>(relaxed = true)
        val clipboardManager = mockk<ClipboardManager>(relaxed = true)
        every { context.getSystemService(Context.CLIPBOARD_SERVICE) } returns clipboardManager

        vm.copyToClipboard(context)
        testDispatcher.scheduler.advanceUntilIdle()

        val content = contentSlot.captured
        assertTrue(content.contains("&lt;script&gt;"))
        assertFalse(content.contains("<script>"))

        unmockkStatic(android.content.ClipData::class)
    }
}
