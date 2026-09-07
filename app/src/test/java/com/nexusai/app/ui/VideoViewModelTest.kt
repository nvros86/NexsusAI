package com.nexusai.app.ui

import android.app.Application
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class VideoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: Application
    private lateinit var viewModel: VideoViewModel

    private fun createVideo(
        id: String = "video-1",
        prompt: String = "Test video",
        isFavorite: Boolean = false
    ) = GeneratedVideo(
        id = id,
        prompt = prompt,
        isFavorite = isFavorite
    )

    @Suppress("UNCHECKED_CAST")
    private fun setStateVideos(videos: List<GeneratedVideo>) {
        val field = viewModel.javaClass.getDeclaredField("_uiState")
        field.isAccessible = true
        val stateFlow = field.get(viewModel) as MutableStateFlow<VideoGenUiState>
        stateFlow.value = stateFlow.value.copy(videos = videos)
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = mockk(relaxed = true)
        every { application.getString(any<Int>()) } returns "Video generation not supported"
        viewModel = VideoViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        val state = viewModel.uiState.value
        assertEquals("", state.prompt)
        assertFalse(state.isGenerating)
        assertTrue(state.videos.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `setPrompt updates prompt`() {
        viewModel.setPrompt("A cat dancing")
        assertEquals("A cat dancing", viewModel.uiState.value.prompt)
    }

    @Test
    fun `setPrompt can be called multiple times`() {
        viewModel.setPrompt("First")
        viewModel.setPrompt("Second")
        assertEquals("Second", viewModel.uiState.value.prompt)
    }

    @Test
    fun `generate does nothing when prompt is blank`() = runTest {
        viewModel.setPrompt("  ")
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.videos.isEmpty())
    }

    @Test
    fun `generate does nothing when prompt is empty`() = runTest {
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.videos.isEmpty())
    }

    @Test
    fun `generate sets error and clears prompt`() = runTest {
        viewModel.setPrompt("Generate a video")
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.prompt)
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `toggleFavorite toggles favorite status of video`() = runTest {
        val video = createVideo(id = "vid-1", isFavorite = false)
        setStateVideos(listOf(video))

        viewModel.toggleFavorite("vid-1")
        assertTrue(viewModel.uiState.value.videos[0].isFavorite)

        viewModel.toggleFavorite("vid-1")
        assertFalse(viewModel.uiState.value.videos[0].isFavorite)
    }

    @Test
    fun `toggleFavorite does nothing for nonexistent id`() = runTest {
        val video = createVideo(id = "vid-1")
        setStateVideos(listOf(video))

        viewModel.toggleFavorite("nonexistent")
        assertFalse(viewModel.uiState.value.videos[0].isFavorite)
    }

    @Test
    fun `deleteVideo removes video from list`() = runTest {
        val videos = listOf(
            createVideo(id = "vid-1"),
            createVideo(id = "vid-2")
        )
        setStateVideos(videos)

        viewModel.deleteVideo("vid-1")

        assertEquals(1, viewModel.uiState.value.videos.size)
        assertEquals("vid-2", viewModel.uiState.value.videos[0].id)
    }

    @Test
    fun `deleteVideo does nothing for nonexistent id`() = runTest {
        val video = createVideo(id = "vid-1")
        setStateVideos(listOf(video))

        viewModel.deleteVideo("nonexistent")
        assertEquals(1, viewModel.uiState.value.videos.size)
    }

    @Test
    fun `clearVideos resets videos list`() = runTest {
        val videos = listOf(createVideo(id = "1"), createVideo(id = "2"))
        setStateVideos(videos)

        viewModel.clearVideos()
        assertTrue(viewModel.uiState.value.videos.isEmpty())
    }

    @Test
    fun `clearVideos on empty list does nothing`() {
        assertTrue(viewModel.uiState.value.videos.isEmpty())
        viewModel.clearVideos()
        assertTrue(viewModel.uiState.value.videos.isEmpty())
    }

    @Test
    fun `dismissError clears error`() = runTest {
        viewModel.setPrompt("  ")
        viewModel.generate()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.dismissError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `dismissError on null error does nothing`() {
        assertNull(viewModel.uiState.value.error)
        viewModel.dismissError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `GeneratedVideo has correct defaults`() {
        val video = GeneratedVideo(id = "1", prompt = "test")
        assertFalse(video.isFavorite)
        assertNotNull(video.createdAt)
        assertTrue(video.createdAt > 0)
    }
}
