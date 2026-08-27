package com.nexsusai.workstation.ui.workspace

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AIWorkspaceViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(AIWorkspaceState())
    val state: StateFlow<AIWorkspaceState> = _state

    fun updateInput(text: String) {
        _state.value = _state.value.copy(inputText = text)
    }

    fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isEmpty()) return
        val message = ChatMessage(text = text, isUser = true)
        _state.value = _state.value.copy(
            messages = _state.value.messages + message,
            inputText = ""
        )
    }
}
