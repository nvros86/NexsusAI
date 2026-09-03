package com.nexusai.app.ui

import android.app.Application
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import com.nexusai.core.ui.components.VoiceHelper
import com.nexusai.core.ui.components.VoiceState
import com.nexusai.data.ai.AIProviderManager
import com.nexusai.domain.ai.ChatMessage
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.ProviderType
import com.nexusai.domain.repository.AIProviderRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkConstructor
import io.mockk.unmockkStatic
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
class VoiceModeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: Application
    private lateinit var providerRepository: AIProviderRepository
    private lateinit var aiProviderManager: AIProviderManager
    private lateinit var viewModel: VoiceModeViewModel

    private val providersFlow = MutableStateFlow<List<AIProviderConfig>>(emptyList())

    private fun createProvider(
        id: String = "provider-1",
        name: String = "Test Provider",
        apiKey: String = "test-key",
        defaultModel: String = "gpt-4",
        models: List<String> = listOf("gpt-4", "gpt-3.5")
    ) = AIProviderConfig(
        id = id,
        name = name,
        type = ProviderType.OPENAI,
        baseUrl = "https://api.openai.com",
        apiKey = apiKey,
        models = models,
        defaultModel = defaultModel
    )

    private fun replaceVoiceHelperWithMock(): VoiceHelper {
        val mockHelper = mockk<VoiceHelper>(relaxed = true)
        every { mockHelper.state } returns MutableStateFlow(VoiceState.IDLE)
        every { mockHelper.transcript } returns MutableStateFlow("")
        every { mockHelper.partialResult } returns MutableStateFlow("")
        every { mockHelper.amplitude } returns MutableStateFlow(0f)
        every { mockHelper.error } returns MutableStateFlow(null)

        val field = viewModel.javaClass.getDeclaredField("voiceHelper")
        field.isAccessible = true
        field.set(viewModel, mockHelper)

        return mockHelper
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        application = mockk(relaxed = true)
        providerRepository = mockk(relaxed = true)
        aiProviderManager = mockk(relaxed = true)

        every { providerRepository.getAllProviders() } returns providersFlow

        mockkStatic(SpeechRecognizer::class)
        every { SpeechRecognizer.createSpeechRecognizer(any()) } returns mockk(relaxed = true)
        mockkConstructor(TextToSpeech::class)
        every { anyConstructed<TextToSpeech>().setOnUtteranceProgressListener(any()) } returns 0

        viewModel = VoiceModeViewModel(application, providerRepository, aiProviderManager)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        unmockkStatic(SpeechRecognizer::class)
        unmockkConstructor(TextToSpeech::class)
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        val state = viewModel.uiState.value
        assertEquals(VoiceState.IDLE, state.voiceState)
        assertEquals("", state.transcript)
        assertEquals("", state.partialResult)
        assertTrue(state.messages.isEmpty())
        assertEquals(0f, state.amplitude)
        assertNull(state.error)
        assertNull(state.selectedProvider)
        assertTrue(state.autoSpeak)
    }

    @Test
    fun `init loads providers from repository`() = runTest {
        val provider = createProvider()
        providersFlow.value = listOf(provider)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.selectedProvider)
        assertEquals("provider-1", state.selectedProvider!!.id)
    }

    @Test
    fun `init selects first provider with apiKey`() = runTest {
        val withKey = createProvider(id = "p1", apiKey = "key-1")
        val withoutKey = createProvider(id = "p2", name = "No Key", apiKey = "")
        providersFlow.value = listOf(withoutKey, withKey)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("p1", viewModel.uiState.value.selectedProvider!!.id)
    }

    @Test
    fun `processUserInput adds user message and sets THINKING state`() = runTest {
        val mockHelper = replaceVoiceHelperWithMock()
        val provider = createProvider()
        providersFlow.value = listOf(provider)
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery {
            aiProviderManager.getProvider(any())
        } returns mockk {
            coEvery {
                sendMessage(any(), any(), any(), any())
            } returns mockk { every { content } returns "Hello from AI" }
        }

        viewModel.processUserInput("Hi there")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.messages.size)
        assertEquals("Hi there", state.messages[0].text)
        assertTrue(state.messages[0].isUser)
        assertEquals("Hello from AI", state.messages[1].text)
        assertFalse(state.messages[1].isUser)
        assertEquals("", state.transcript)
    }

    @Test
    fun `processUserInput adds AI response when provider is set`() = runTest {
        val mockHelper = replaceVoiceHelperWithMock()
        val provider = createProvider()
        providersFlow.value = listOf(provider)
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery {
            aiProviderManager.getProvider(any())
        } returns mockk {
            coEvery {
                sendMessage(any(), any(), any(), any())
            } returns mockk { every { content } returns "AI reply" }
        }

        viewModel.processUserInput("Question")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.messages.size)
        assertEquals("Question", state.messages[0].text)
        assertTrue(state.messages[0].isUser)
        assertEquals("AI reply", state.messages[1].text)
        assertFalse(state.messages[1].isUser)
    }

    @Test
    fun `processUserInput without provider adds fallback message`() = runTest {
        val mockHelper = replaceVoiceHelperWithMock()
        providersFlow.value = emptyList()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.processUserInput("Hello")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.messages.size)
        assertEquals("Hello", state.messages[0].text)
        assertTrue(state.messages[0].isUser)
        assertTrue(state.messages[1].text.isNotBlank())
        assertFalse(state.messages[1].isUser)
        assertEquals(VoiceState.IDLE, state.voiceState)
    }

    @Test
    fun `processUserInput sets ERROR state on exception`() = runTest {
        val mockHelper = replaceVoiceHelperWithMock()
        val provider = createProvider()
        providersFlow.value = listOf(provider)
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery {
            aiProviderManager.getProvider(any())
        } throws RuntimeException("API error")

        viewModel.processUserInput("Test")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertEquals(VoiceState.ERROR, state.voiceState)
    }

    @Test
    fun `processUserInput with autoSpeak false sets IDLE after response`() = runTest {
        val mockHelper = replaceVoiceHelperWithMock()
        val provider = createProvider()
        providersFlow.value = listOf(provider)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleAutoSpeak()
        assertFalse(viewModel.uiState.value.autoSpeak)

        coEvery {
            aiProviderManager.getProvider(any())
        } returns mockk {
            coEvery {
                sendMessage(any(), any(), any(), any())
            } returns mockk { every { content } returns "Response" }
        }

        viewModel.processUserInput("Hello")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(VoiceState.IDLE, viewModel.uiState.value.voiceState)
    }

    @Test
    fun `toggleAutoSpeak toggles autoSpeak flag`() {
        assertTrue(viewModel.uiState.value.autoSpeak)

        viewModel.toggleAutoSpeak()
        assertFalse(viewModel.uiState.value.autoSpeak)

        viewModel.toggleAutoSpeak()
        assertTrue(viewModel.uiState.value.autoSpeak)
    }

    @Test
    fun `clearMessages resets messages list`() = runTest {
        val mockHelper = replaceVoiceHelperWithMock()
        val provider = createProvider()
        providersFlow.value = listOf(provider)
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery {
            aiProviderManager.getProvider(any())
        } returns mockk {
            coEvery {
                sendMessage(any(), any(), any(), any())
            } returns mockk { every { content } returns "Reply" }
        }

        viewModel.processUserInput("Hi")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.messages.isNotEmpty())

        viewModel.clearMessages()
        assertTrue(viewModel.uiState.value.messages.isEmpty())
    }

    @Test
    fun `setProvider updates selectedProvider`() {
        val provider = createProvider(id = "new-provider", name = "New Provider")
        viewModel.setProvider(provider)

        assertEquals("new-provider", viewModel.uiState.value.selectedProvider!!.id)
        assertEquals("New Provider", viewModel.uiState.value.selectedProvider!!.name)
    }

    @Test
    fun `dismissError clears error and resets voiceHelper`() {
        val mockHelper = replaceVoiceHelperWithMock()

        viewModel.dismissError()
        assertNull(viewModel.uiState.value.error)
        io.mockk.verify { mockHelper.reset() }
    }

    @Test
    fun `toggleListening in IDLE state starts listening`() {
        val mockHelper = replaceVoiceHelperWithMock()
        every { mockHelper.state } returns MutableStateFlow(VoiceState.IDLE)

        viewModel.toggleListening()

        io.mockk.verify { mockHelper.startListening() }
    }

    @Test
    fun `toggleListening in LISTENING state stops listening`() {
        val mockHelper = replaceVoiceHelperWithMock()
        every { mockHelper.state } returns MutableStateFlow(VoiceState.LISTENING)

        viewModel.toggleListening()

        io.mockk.verify { mockHelper.stopListening() }
    }

    @Test
    fun `toggleListening in SPEAKING state stops speaking and starts listening`() {
        val mockHelper = replaceVoiceHelperWithMock()
        every { mockHelper.state } returns MutableStateFlow(VoiceState.SPEAKING)

        viewModel.toggleListening()

        io.mockk.verify { mockHelper.stopSpeaking() }
        io.mockk.verify { mockHelper.startListening() }
    }

    @Test
    fun `toggleListening in other state resets`() {
        val mockHelper = replaceVoiceHelperWithMock()
        every { mockHelper.state } returns MutableStateFlow(VoiceState.THINKING)

        viewModel.toggleListening()

        io.mockk.verify { mockHelper.reset() }
    }
}
