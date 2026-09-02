package com.nexusai.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.app.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(VideoGenUiState())
    val uiState: StateFlow<VideoGenUiState> = _uiState.asStateFlow()

    fun setPrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(prompt = prompt)
    }

    fun generate() {
        val prompt = _uiState.value.prompt
        if (prompt.isBlank()) return

        val context = getApplication<Application>()
        _uiState.value = _uiState.value.copy(
            prompt = "",
            error = context.getString(R.string.video_not_supported)
        )
    }

    fun toggleFavorite(id: String) {
        _uiState.value = _uiState.value.copy(
            videos = _uiState.value.videos.map {
                if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
            }
        )
    }

    fun deleteVideo(id: String) {
        _uiState.value = _uiState.value.copy(
            videos = _uiState.value.videos.filter { it.id != id }
        )
    }

    fun clearVideos() {
        _uiState.value = _uiState.value.copy(videos = emptyList())
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
