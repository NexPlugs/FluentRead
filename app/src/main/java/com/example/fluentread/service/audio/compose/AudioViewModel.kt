package com.example.fluentread.service.audio.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AudioDataState(
    val isRecording: Boolean = false,
    val amplitudeTracking: Float = 0f,
)

class AudioViewModel @Inject constructor() : ViewModel() {
    private  var _uiState: MutableStateFlow<AudioDataState> = MutableStateFlow(AudioDataState())
    val uiState: StateFlow<AudioDataState> = _uiState

    fun setIsRecording(isRecording: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(isRecording = isRecording)
        }
    }

    fun setAmplitudeTracking(amplitude: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(amplitudeTracking = amplitude)
        }
    }

}