package com.example.fluentread.service.overlay.screenRecordCompose

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


data class ScreenRecordDataView(
    val isRecording: Boolean = true,
    val recordingTime: Long = 0L
)

class ScreenRecordViewModel @Inject constructor(): ViewModel() {

    private val _uiState: MutableStateFlow<ScreenRecordDataView> = MutableStateFlow(ScreenRecordDataView())
    val state: StateFlow<ScreenRecordDataView> = _uiState


    fun setIsRecording(isRecording: Boolean) {
        _uiState.update {
            it.copy(isRecording = isRecording)
        }
    }

    fun setRecordingTime(recordingTime: Long) {
        _uiState.update {
            it.copy(recordingTime = recordingTime)
        }
    }
}