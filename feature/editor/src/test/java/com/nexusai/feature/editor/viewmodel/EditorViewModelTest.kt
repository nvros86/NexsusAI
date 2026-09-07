package com.nexusai.feature.editor.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var viewModel: EditorViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = mockk(relaxed = true)
        contentResolver = mockk(relaxed = true)
        every { context.contentResolver } returns contentResolver
        viewModel = EditorViewModel(context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        val state = viewModel.state.value
        assertEquals("", state.content)
        assertEquals("", state.fileName)
        assertEquals("", state.filePath)
        assertFalse(state.isModified)
        assertFalse(state.isReadOnly)
        assertEquals("text", state.language)
        assertEquals(0, state.lineCount)
        assertEquals(1, state.cursorLine)
        assertEquals(1, state.cursorColumn)
    }

    @Test
    fun `updateContent sets content and marks as modified`() {
        viewModel.updateContent("Hello, World!")

        val state = viewModel.state.value
        assertEquals("Hello, World!", state.content)
        assertTrue(state.isModified)
    }

    @Test
    fun `updateContent calculates line count`() {
        viewModel.updateContent("Line 1\nLine 2\nLine 3")
        assertEquals(3, viewModel.state.value.lineCount)
    }

    @Test
    fun `updateContent single line`() {
        viewModel.updateContent("Single line")
        assertEquals(1, viewModel.state.value.lineCount)
    }

    @Test
    fun `updateCursor updates cursor position`() {
        viewModel.updateCursor(5, 10)

        val state = viewModel.state.value
        assertEquals(5, state.cursorLine)
        assertEquals(10, state.cursorColumn)
    }

    @Test
    fun `newFile resets state with defaults`() {
        viewModel.updateContent("Some content")
        assertTrue(viewModel.state.value.isModified)

        viewModel.newFile()

        val state = viewModel.state.value
        assertEquals("untitled.txt", state.fileName)
        assertFalse(state.isModified)
        assertEquals("text", state.language)
        assertEquals(1, state.lineCount)
    }

    @Test
    fun `newFile with custom name sets name and detects language`() {
        viewModel.newFile("script.py")

        val state = viewModel.state.value
        assertEquals("script.py", state.fileName)
        assertEquals("python", state.language)
    }

    @Test
    fun `newFile with kotlin extension detects kotlin language`() {
        viewModel.newFile("Main.kt")
        assertEquals("kotlin", viewModel.state.value.language)
    }

    @Test
    fun `newFile with java extension detects java language`() {
        viewModel.newFile("App.java")
        assertEquals("java", viewModel.state.value.language)
    }

    @Test
    fun `newFile with js extension detects javascript`() {
        viewModel.newFile("index.js")
        assertEquals("javascript", viewModel.state.value.language)
    }

    @Test
    fun `newFile with html extension detects html`() {
        viewModel.newFile("index.html")
        assertEquals("html", viewModel.state.value.language)
    }

    @Test
    fun `newFile with json extension detects json`() {
        viewModel.newFile("config.json")
        assertEquals("json", viewModel.state.value.language)
    }

    @Test
    fun `newFile with xml extension detects xml`() {
        viewModel.newFile("layout.xml")
        assertEquals("xml", viewModel.state.value.language)
    }

    @Test
    fun `newFile with md extension detects markdown`() {
        viewModel.newFile("README.md")
        assertEquals("markdown", viewModel.state.value.language)
    }

    @Test
    fun `newFile with yaml extension detects yaml`() {
        viewModel.newFile("config.yaml")
        assertEquals("yaml", viewModel.state.value.language)
    }

    @Test
    fun `newFile with sh extension detects shell`() {
        viewModel.newFile("build.sh")
        assertEquals("shell", viewModel.state.value.language)
    }

    @Test
    fun `newFile with sql extension detects sql`() {
        viewModel.newFile("query.sql")
        assertEquals("sql", viewModel.state.value.language)
    }

    @Test
    fun `newFile with unknown extension defaults to text`() {
        viewModel.newFile("file.xyz")
        assertEquals("text", viewModel.state.value.language)
    }

    @Test
    fun `saveToFile returns false when filePath is empty`() {
        val result = viewModel.saveToFile()
        assertFalse(result)
    }

    @Test
    fun `loadFromUri reads file content`() {
        val uri = mockk<Uri>()
        val inputStream = ByteArrayInputStream("File content here".toByteArray())
        every { contentResolver.openInputStream(uri) } returns inputStream
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null

        viewModel.loadFromUri(uri)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("File content here", state.content)
        assertFalse(state.isModified)
    }

    @Test
    fun `loadFromUri on exception sets error content`() {
        val uri = mockk<Uri>()
        every { contentResolver.openInputStream(uri) } throws RuntimeException("Permission denied")

        viewModel.loadFromUri(uri)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.content.contains("Error loading file"))
        assertEquals("error.txt", state.fileName)
    }
}
